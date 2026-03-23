package com.eimemes.chat.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatApiService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "ChatApiService"
        private const val BASE_URL = "https://eimemes-chat-ai.vercel.app"
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun streamChat(request: ChatRequest): Flow<StreamState> = callbackFlow {
        trySend(StreamState.Typing)

        val token = try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            trySend(StreamState.Error("Authentication failed. Please sign in again."))
            close()
            return@callbackFlow
        }

        if (token == null) {
            trySend(StreamState.Error("Not signed in."))
            close()
            return@callbackFlow
        }

        val body = Json.encodeToString(ChatRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$BASE_URL/api/chat")
            .post(body)
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .build()

        var accumulated = ""

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") return
                try {
                    val event = json.decodeFromString<SseEvent>(data)
                    when {
                        event.searching == true -> trySend(StreamState.Searching)
                        event.token != null -> {
                            accumulated += event.token
                            trySend(StreamState.Streaming(accumulated))
                        }
                        event.outputBlocked == true && event.safeReply != null -> {
                            accumulated = event.safeReply
                            trySend(StreamState.Streaming(accumulated))
                        }
                        event.done == true -> {
                            trySend(StreamState.Done(
                                text = accumulated,
                                model = event.model ?: "",
                                disclaimer = event.disclaimer,
                                sources = event.sources?.map { SseSource(it.title, it.url) } ?: emptyList()
                            ))
                            close()
                        }
                        event.error != null -> {
                            trySend(StreamState.Error(event.error))
                            close()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Parse error: ${e.message}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val msg = when (response?.code) {
                    429 -> "Daily message limit reached. Resets tomorrow."
                    401 -> "Session expired. Please sign in again."
                    else -> "Connection error. Please try again."
                }
                trySend(StreamState.Error(msg))
                close()
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = EventSources.createFactory(okHttpClient)
            .newEventSource(httpRequest, listener)

        awaitClose { eventSource.cancel() }
    }
}
