package com.example.eclinic.firebase

/**
 * Data class representing a notification within the eClinic application.
 * This class is used for serializing and deserializing notification data to and from Firebase Firestore.
 *
 * @property id The unique ID of the notification document in Firestore.
 * @property userId The Firebase User ID (UID) of the user who is the recipient of this notification.
 * @property message The content of the notification message.
 * @property type The type of the notification (e.g., "APPOINTMENT_REMINDER", "CHAT_MESSAGE").
 * Defaults to "APPOINTMENT_REMINDER".
 * @property timestamp A string representing the time the notification was created.
 * It's recommended to use a more robust type like [com.google.firebase.Timestamp] or [java.util.Date]
 * for accurate chronological sorting and time zone handling in a production environment.
 * @property isRead A boolean indicating whether the user has read or acknowledged the notification.
 * Defaults to `false`.
 */
data class Notification(
    var id: String = "",
    var userId: String = "",
    var message: String = "",
    var type: String = "APPOINTMENT_REMINDER",
    var timestamp: String = "",
    var isRead: Boolean = false
)