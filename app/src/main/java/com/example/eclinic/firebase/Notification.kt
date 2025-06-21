package com.example.eclinic.firebase

data class Notification(
    var id: String = "",
    var userId: String = "",
    var message: String = "",
    var type: String = "APPOINTMENT_REMINDER",
    var timestamp: String = "",
    var isRead: Boolean = false
)