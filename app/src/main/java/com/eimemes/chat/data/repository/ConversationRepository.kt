package com.eimemes.chat.data.repository

import com.eimemes.chat.domain.model.Conversation
import com.eimemes.chat.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private fun uid() = auth.currentUser?.uid ?: throw Exception("Not authenticated")

    private fun convsRef() = firestore.collection("users").document(uid()).collection("conversations")

    fun observeConversations(): Flow<List<Conversation>> = callbackFlow {
        val listener = convsRef()
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    runCatching { docToConversation(doc.id, doc.data ?: emptyMap()) }.getOrNull()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getConversation(convId: String): Conversation? {
        val doc = convsRef().document(convId).get().await()
        return runCatching { docToConversation(doc.id, doc.data ?: emptyMap()) }.getOrNull()
    }

    suspend fun createConversation(): String {
        val ref = convsRef().document()
        val now = System.currentTimeMillis()
        ref.set(mapOf(
            "title"     to "New conversation",
            "createdAt" to now,
            "updatedAt" to now,
            "messages"  to emptyList<Any>()
        )).await()
        return ref.id
    }

    suspend fun saveMessage(convId: String, message: Message, newTitle: String? = null) {
        val ref = convsRef().document(convId)
        val doc = ref.get().await()
        val existingMessages = (doc.data?.get("messages") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
        val updatedMessages = existingMessages + messageToMap(message)
        val update = mutableMapOf<String, Any>(
            "messages"  to updatedMessages,
            "updatedAt" to System.currentTimeMillis()
        )
        newTitle?.let { update["title"] = it }
        ref.update(update).await()
    }

    suspend fun deleteConversation(convId: String) {
        convsRef().document(convId).delete().await()
    }

    suspend fun clearAllConversations() {
        val docs = convsRef().get().await()
        val batch = firestore.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    // ── Usage counter ──────────────────────────────────────────────
    suspend fun incrementUsageCounter(): Int {
        val userRef = firestore.collection("users").document(uid())
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return firestore.runTransaction { tx ->
            val snap = tx.get(userRef)
            val lastDate = snap.getString("lastDate") ?: ""
            val count = if (lastDate == today) (snap.getLong("dailyCount") ?: 0L).toInt() else 0
            val newCount = count + 1
            tx.set(userRef, mapOf("dailyCount" to newCount, "lastDate" to today), com.google.firebase.firestore.SetOptions.merge())
            newCount
        }.await()
    }

    // ── Helpers ────────────────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    private fun docToConversation(id: String, data: Map<String, Any>): Conversation {
        val rawMessages = (data["messages"] as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
        return Conversation(
            id        = id,
            title     = data["title"] as? String ?: "New conversation",
            createdAt = (data["createdAt"] as? Long) ?: 0L,
            updatedAt = (data["updatedAt"] as? Long) ?: 0L,
            messages  = rawMessages.mapNotNull { mapToMessage(it) }
        )
    }

    private fun mapToMessage(m: Map<String, Any>): Message? {
        val role    = m["role"] as? String ?: return null
        val content = m["content"] as? String ?: return null
        return Message(
            role       = role,
            content    = content,
            time       = m["time"] as? String ?: "",
            model      = m["model"] as? String,
            disclaimer = m["disclaimer"] as? String
        )
    }

    private fun messageToMap(msg: Message): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "role"    to msg.role,
            "content" to msg.content,
            "time"    to msg.time
        )
        msg.model?.let      { map["model"] = it }
        msg.disclaimer?.let { map["disclaimer"] = it }
        msg.sources?.let    { map["sources"] = it.map { s -> mapOf("title" to s.title, "url" to s.url) } }
        return map
    }
}
