package com.example.eclinic.chat

import com.google.firebase.firestore.FirebaseFirestore

object ChatUtils {
    fun sendMessage(fromId: String, toId: String, messageText: String) {
        val firestore = FirebaseFirestore.getInstance()

        val participantsSorted = listOf(fromId, toId).sorted()
        val generatedChatId = "${participantsSorted[0]}_${participantsSorted[1]}"
        val chatRef = firestore.collection("chats").document(generatedChatId)

        chatRef.get().addOnSuccessListener { docSnapshot ->
            if (!docSnapshot.exists()) {
                val newChatData = hashMapOf(
                    "participants" to participantsSorted,
                    "lastMessageTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "lastMessageText" to messageText
                )
                chatRef.set(newChatData)
            } else {
                chatRef.update(
                    "lastMessageTimestamp", com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "lastMessageText", messageText
                )
            }

            val message = ChatMessage(
                senderId = fromId,
                receiverId = toId,
                messageText = messageText
            )

            chatRef.collection("messages")
                .add(message)
        }
    }
}
