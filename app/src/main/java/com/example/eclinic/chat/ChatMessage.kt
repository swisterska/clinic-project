package com.example.eclinic.chat

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class representing a single chat message exchanged between users.
 *
 * @property id Unique identifier of the message document in Firestore (automatically assigned).
 * @property senderId User ID of the message sender.
 * @property receiverId User ID of the message receiver.
 * @property messageText The content of the chat message.
 * @property timestamp Server-generated timestamp indicating when the message was sent.
 */
data class ChatMessage(
    @DocumentId
    val id: String? = null,
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
