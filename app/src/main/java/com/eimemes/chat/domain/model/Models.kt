package com.eimemes.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String = "",
    val content: String = "",
    val time: String = "",
    val model: String = "",
    val disclaimer: String? = null,   // "critical" | "web" | null
    val sources: List<Source>? = null,
    val attachment: AttachmentMeta? = null
)

@Serializable
data class Source(
    val title: String = "",
    val url: String = ""
)

@Serializable
data class AttachmentMeta(
    val name: String = "",
    val type: String = ""
)

@Serializable
data class Conversation(
    val id: String = "",
    val title: String = "New conversation",
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserPreferences(
    val tone: String = "Friendly",
    val nickname: String = "",
    val occupation: String = "",
    val customInstructions: String = ""
)

data class ChatAttachment(
    val name: String,
    val type: AttachmentType,
    val mimeType: String,
    val content: String   // base64 for images, extracted text for docs
)

enum class AttachmentType { IMAGE, PDF, TEXT, DOCX }
