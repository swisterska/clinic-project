package com.example.eclinic.chat

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatMessage(
    @DocumentId
    val id: String? = null,
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
