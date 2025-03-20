package com.example.eclinic.firebase

data class Patient(
    var id: String = "",
    var user: User, // Links to a User object
    var dateOfBirth: String = "",
    var medicalHistory: List<String> = emptyList()
)