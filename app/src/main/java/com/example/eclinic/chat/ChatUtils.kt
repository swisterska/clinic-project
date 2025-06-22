package com.example.eclinic.chat

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Utility object for handling chat-related operations, specifically sending messages.
 * This object provides a static method to send a chat message between two users
 * and manage the corresponding chat document in Firebase Firestore.
 */
object ChatUtils {
    /**
     * Sends a chat message from one user to another.
     * This function ensures that a chat document exists between the two participants.
     * If the chat does not exist, it creates a new one. If it exists, it updates its metadata.
     * Finally, it adds the new message to the chat's messages subcollection.
     *
     * @param fromId The Firebase User ID (UID) of the sender.
     * @param toId The Firebase User ID (UID) of the recipient.
     * @param messageText The content of the message to be sent.
     */
    fun sendMessage(fromId: String, toId: String, messageText: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Create a consistent chat ID by sorting the participant IDs
        val participantsSorted = listOf(fromId, toId).sorted()
        val generatedChatId = "${participantsSorted[0]}_${participantsSorted[1]}"
        val chatRef = firestore.collection("chats").document(generatedChatId)

        // Check if the chat document exists
        chatRef.get().addOnSuccessListener { docSnapshot ->
            if (!docSnapshot.exists()) {
                // If chat does not exist, create a new one
                val newChatData = hashMapOf(
                    "participants" to participantsSorted,
                    "lastMessageTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "lastMessageText" to messageText
                )
                chatRef.set(newChatData)
            } else {
                // If chat exists, update its last message timestamp and text
                chatRef.update(
                    "lastMessageTimestamp", com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "lastMessageText", messageText
                )
            }

            // Create the chat message object
            val message = ChatMessage(
                senderId = fromId,
                receiverId = toId,
                messageText = messageText
            )

            // Add the message to the 'messages' subcollection of the chat
            chatRef.collection("messages")
                .add(message)
        }
    }
}