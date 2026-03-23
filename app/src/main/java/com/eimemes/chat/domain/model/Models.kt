package com.eimemes.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String,
    val content: String,
    val time: String,
    val model: String? = null,
    val disclaimer: String? = null,
    val sources: List<Source>? = null
)

@Serializable
data class Source(
    val title: String,
    val url: String
)

@Serializable
data class Conversation(
    val id: String = "",
    val title: String = "New conversation",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val messages: List<Message> = emptyList()
)

@Serializable
data class UserPreferences(
    val tone: String = "Friendly",
    val nickname: String = "",
    val occupation: String = "",
    val customInstructions: String = ""
)

data class Attachment(
    val name: String,
    val type: AttachmentType,
    val mimeType: String,
    val content: String
)

enum class AttachmentType { IMAGE, PDF, TEXT, DOCX }
