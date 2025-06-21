package com.example.eclinic.firebase

data class Prescription(
    val doctorId: String = "",
    val patientId: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val url: String = "",
    )
