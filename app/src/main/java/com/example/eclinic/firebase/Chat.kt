package com.example.eclinic.firebase

/**
 * Data class representing a chat or message entity in the eClinic system.
 * This class is likely used to structure chat messages stored or retrieved from a database.
 *
 * @property id The unique identifier of the chat message.
 * @property patientId The ID of the patient involved in this chat.
 * @property doctorId The ID of the doctor involved in this chat.
 * @property message The actual text content of the chat message.
 * @property timestamp The timestamp when the message was sent or received, typically in a string format.
 */
data class Chat(
    var id: String = "",
    var patientId: String = "",
    var doctorId: String = "",
    var message: String = "",
    var timestamp: String = ""
)