package com.example.eclinic.firebase

/**
 * Data class representing a notification in the eClinic system.
 * This class encapsulates the details of a notification, such as its content,
 * target user, type, and read status.
 *
 * @property id The unique identifier of the notification.
 * @property userId The ID of the user who is the recipient of this notification.
 * @property message The content of the notification message.
 * @property type The type of the notification, defaulting to "APPOINTMENT_REMINDER".
 * This can be used to categorize notifications (e.g., "APPOINTMENT_REMINDER", "PRESCRIPTION_ISSUED", "CHAT_MESSAGE").
 * @property timestamp The timestamp when the notification was created or sent, typically in a string format.
 * @property isRead A boolean indicating whether the notification has been read by the user, defaulting to `false`.
 */
data class Notification(
    var id: String = "",
    var userId: String = "",
    var message: String = "",
    var type: String = "APPOINTMENT_REMINDER",
    var timestamp: String = "",
    var isRead: Boolean = false
)