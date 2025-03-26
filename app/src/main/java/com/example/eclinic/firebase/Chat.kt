package com.example.eclinic.firebase

data class Chat(
    var id: String = "",
    var patientId: String = "",
    var doctorId: String = "",
    var message: String = "",
    var timestamp: String = ""
)