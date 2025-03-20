package com.example.eclinic.firebase

data class Doctor(
    var id: String = "",
    var user: User, // Links to a User object
    var specialization: String = "",
    var phoneNumber: String = "",
    var bio: String = ""
)