package com.eimemes.chat.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.eimemes.chat.domain.model.Attachment
import com.eimemes.chat.domain.model.AttachmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object FilePickerUtil {

    /** MIME types accepted by the file picker */
    val ACCEPTED_MIME_TYPES = arrayOf(
        "image/jpeg", "image/png", "image/webp", "image/gif",
        "application/pdf",
        "text/plain", "text/markdown", "text/csv"
    )

    /**
     * Reads a URI selected by the user and returns an [Attachment].
     * Images are base64-encoded. Text files are read as plain text.
     * Max file size: 5MB.
     */
    suspend fun processUri(context: Context, uri: Uri): Result<Attachment> = withContext(Dispatchers.IO) {
        runCatching {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val displayName = queryDisplayName(context, uri) ?: uri.lastPathSegment ?: "file"

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Could not read file")

            if (bytes.size > 5 * 1024 * 1024) throw IllegalStateException("File too large (max 5 MB)")

            val (type, content) = when {
                mimeType.startsWith("image/") -> {
                    // Resize to max 1024px wide before base64-encoding
                    val resized = resizeImageBytes(bytes, 1024)
                    AttachmentType.IMAGE to Base64.encodeToString(resized, Base64.NO_WRAP)
                }
                mimeType == "application/pdf" -> {
                    AttachmentType.PDF to Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
                mimeType.startsWith("text/") -> {
                    AttachmentType.TEXT to bytes.decodeToString()
                }
                else -> throw IllegalStateException("Unsupported file type: $mimeType")
            }

            Attachment(name = displayName, type = type, mimeType = mimeType, content = content)
        }
    }

    private fun resizeImageBytes(bytes: ByteArray, maxWidth: Int): ByteArray {
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return bytes
        if (original.width <= maxWidth) return bytes
        val scale = maxWidth.toFloat() / original.width
        val resized = android.graphics.Bitmap.createScaledBitmap(
            original,
            maxWidth,
            (original.height * scale).toInt(),
            true
        )
        val out = ByteArrayOutputStream()
        resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        return out.toByteArray()
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }
}
