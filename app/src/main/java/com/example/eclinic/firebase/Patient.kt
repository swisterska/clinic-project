package com.example.eclinic.firebase

data class Patient(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var dob: String = "",  // Date of Birth (e.g. "1995-06-15")
    var profilePicUrl: String = "",  // URL to Firebase Storage image
    var phoneNumber: String = "",
    var appointments: List<String> = emptyList() // List of upcoming appointment IDs
    // what will have to be done is moving the appointment from appointments list to the medical history
    // sub-collection when it is completed
)

data class MedicalHistory(
    var id: String = "",
    var appointmentId: String = "",
    var doctorId: String = "",
    var diagnosis: String = "",
    var prescriptionId: String = "",
    var visitDate: String = "",
    var doctorNotes: String? = null
)
