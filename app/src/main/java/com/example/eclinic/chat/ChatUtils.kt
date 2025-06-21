package com.example.eclinic.chat

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Utility object for chat-related helper functions.
 */
object ChatUtils {

    /**
     * Sends a chat message from one user to another.
     *
     * This function:
     * - Generates a deterministic chat ID by sorting the two participant IDs.
     * - Checks if a chat document already exists in Firestore.
     * - If it does not exist, creates a new chat document with participants and initial metadata.
     * - If it exists, updates the last message timestamp and text.
     * - Adds the new message to the "messages" subcollection inside the chat document.
     *
     * @param fromId The sender's user ID.
     * @param toId The receiver's user ID.
     * @param messageText The text content of the message to send.
     */
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
