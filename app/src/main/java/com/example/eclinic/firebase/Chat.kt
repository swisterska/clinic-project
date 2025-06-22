package com.example.eclinic.firebase

/**
 * Data class representing a chat message or a simplified chat overview (if used for last message).
 * This class is intended for serializing and deserializing chat data to and from Firebase Firestore.
 *
 * @property id The unique ID of the chat document or message.
 * @property patientId The Firebase User ID (UID) of the patient involved in the chat.
 * @property doctorId The Firebase User ID (UID) of the doctor involved in the chat.
 * @property message The content of the chat message.
 * @property timestamp A string representing the time the message was sent.
 * Consider using a more robust type like [com.google.firebase.Timestamp] or [java.util.Date]
 * for accurate chronological sorting and time zone handling.
 */
data class Chat(
    var id: String = "",
    var patientId: String = "",
    var doctorId: String = "",
    var message: String = "",
    var timestamp: String = ""
)