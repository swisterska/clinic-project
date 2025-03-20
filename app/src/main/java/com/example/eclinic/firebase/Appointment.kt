package com.example.eclinic.firebase

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

data class Appointment(
    var id: String = "",
    var doctorId: String = "",
    var patientId: String = "",
    var dateTime: String = "",
    var status: AppointmentStatus = AppointmentStatus.PENDING,
    var reason: String = "",
    var prescriptionId: String? = null // If a prescription was issued
)