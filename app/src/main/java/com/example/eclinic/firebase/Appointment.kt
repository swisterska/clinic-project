package com.example.eclinic.firebase

/**
 * Enum class representing the possible statuses of an appointment.
 * - [PENDING]: The appointment request is awaiting confirmation.
 * - [CONFIRMED]: The appointment has been confirmed by the doctor.
 * - [COMPLETED]: The appointment has taken place.
 * - [CANCELLED]: The appointment has been cancelled by either party.
 */
enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

/**
 * Data class representing an appointment in the eClinic system.
 *
 * @property id The unique identifier of the appointment.
 * @property doctorId The ID of the doctor involved in the appointment.
 * @property patientId The ID of the patient involved in the appointment.
 * @property dateTime The date and time of the appointment, typically in a string format (e.g., "YYYY-MM-DD HH:MM").
 * @property status The current status of the appointment, defaulting to [AppointmentStatus.PENDING].
 * @property prescriptionId Optional. The ID of the prescription issued during this appointment, if any.
 * @property chatId Optional. The ID of the chat associated with this appointment.
 */
data class Appointment(
    var id: String = "",
    var doctorId: String = "",
    var patientId: String = "",
    var dateTime: String = "",
    var status: AppointmentStatus = AppointmentStatus.PENDING,
    var prescriptionId: String? = null, // If a prescription was issued
    var chatId: String? = null
)