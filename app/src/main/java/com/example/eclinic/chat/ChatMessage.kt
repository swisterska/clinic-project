package com.example.eclinic.chat

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class representing a single chat message in the eClinic application.
 * This class is used for serializing and deserializing chat messages to and from Firebase Firestore.
 *
 * @property id The unique ID of the document in Firestore where this message is stored.
 * This is automatically populated by Firestore's `@DocumentId` annotation.
 * @property senderId The Firebase User ID (UID) of the user who sent this message.
 * @property receiverId The Firebase User ID (UID) of the user who is intended to receive this message.
 * @property messageText The actual text content of the chat message.
 * @property messageType The type of the message, e.g., "text" or "file". Defaults to "text".
 * @property fileUrl An optional URL to the file if the message includes an attachment (e.g., PDF, image).
 * @property timestamp The server timestamp when the message was sent.
 * This is automatically populated by Firestore's `@ServerTimestamp` annotation,
 * ensuring accurate and consistent timekeeping across clients.
 */
data class ChatMessage(
    @DocumentId
    val id: String? = null,
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val messageType: String = "text",
    val fileUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)