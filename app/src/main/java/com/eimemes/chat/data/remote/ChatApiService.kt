package com.eimemes.chat.data.remote

import com.eimemes.chat.domain.model.Attachment
import com.eimemes.chat.domain.model.Message
import com.eimemes.chat.domain.model.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import javax.inject.Singleton

sealed class StreamEvent {
    data class Token(val text: String) : StreamEvent()
    data object Searching : StreamEvent()
    data class Done(val model: String, val disclaimer: String?, val sources: List<Source>?) : StreamEvent()
    data class Title(val title: String) : StreamEvent()
    data class OutputBlocked(val reply: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
}

@Singleton
class ChatApiService @Inject constructor(private val client: OkHttpClient) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val API_BASE = "https://eimemes-chat-ai.vercel.app"

    fun streamChat(
        message: String,
        history: List<Message>,
        isFirstMessage: Boolean,
        idToken: String,
        attachment: Attachment? = null,
        useWebSearch: Boolean = false
    ): Flow<StreamEvent> = callbackFlow {

        val bodyMap = buildJsonObject {
            put("message", message)
            put("isFirstMessage", isFirstMessage)
            put("useWebSearch", useWebSearch)
            putJsonArray("history") {
                history.takeLast(20).forEach { msg ->
                    addJsonObject {
                        put("role", msg.role)
                        put("content", msg.content)
                    }
                }
            }
            attachment?.let {
                putJsonObject("attachment") {
                    put("name", it.name)
                    put("type", it.type.name.lowercase())
                    put("mimeType", it.mimeType)
                    put("content", it.content)
                }
            }
        }

        val request = Request.Builder()
            .url("$API_BASE/api/chat")
            .addHeader("Authorization", "Bearer $idToken")
            .addHeader("Accept", "text/event-stream")
            .post(json.encodeToString(JsonObject.serializer(), bodyMap).toRequestBody("application/json".toMediaType()))
            .build()

        val factory = EventSources.createFactory(client)
        val source = factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val parsed = json.parseToJsonElement(data).jsonObject
                    when {
                        parsed["error"] != null -> trySend(StreamEvent.Error(parsed["error"]!!.jsonPrimitive.content))
                        parsed["token"] != null -> trySend(StreamEvent.Token(parsed["token"]!!.jsonPrimitive.content))
                        parsed["searching"] != null -> trySend(StreamEvent.Searching)
                        parsed["outputBlocked"] != null -> trySend(StreamEvent.OutputBlocked(parsed["safeReply"]?.jsonPrimitive?.contentOrNull ?: ""))
                        parsed["title"] != null -> trySend(StreamEvent.Title(parsed["title"]!!.jsonPrimitive.content))
                        parsed["done"] != null -> {
                            val model = parsed["model"]?.jsonPrimitive?.contentOrNull ?: ""
                            val disclaimer = parsed["disclaimer"]?.jsonPrimitive?.contentOrNull
                            val sources = parsed["sources"]?.jsonArray?.map {
                                val o = it.jsonObject
                                Source(title = o["title"]?.jsonPrimitive?.content ?: "", url = o["url"]?.jsonPrimitive?.content ?: "")
                            }
                            trySend(StreamEvent.Done(model, disclaimer, sources))
                        }
                    }
                } catch (_: Exception) {}
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                trySend(StreamEvent.Error(t?.message ?: "Connection failed"))
                close()
            }

            override fun onClosed(eventSource: EventSource) { close() }
        })

        awaitClose { source.cancel() }
    }
}
