package com.example.eclinic.firebase

data class Patient(
    var id: String = "",
    var user: User, // Links to a User object
    var dateOfBirth: String = "",
    val fcmToken: String = "",
    var medicalHistory: List<String> = emptyList(),
    var profilePictureUrl: String = "",
    var phoneNumber: String = ""

)