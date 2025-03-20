package com.example.eclinic.firebase

data class Prescription(
    var id: String = "",
    var doctorId: String = "",
    var patientId: String = "",
    var medication: List<String> = emptyList(),
    var dosageInstructions: String = "",
    var dateIssued: String = ""
)