package com.example.eclinic.firebase

data class Doctor(
    var id: String = "",
    var user: User, // Links to a User object
    var specialization: String = "",
    var phoneNumber: String = "",
    var bio: String = "",
    var profilePicture: String? = null,
    var email: String = ""
)

data class Availability(
    var id: String = "",
    var date: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var isBooked: Boolean = false
)
