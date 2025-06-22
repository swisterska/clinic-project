package com.example.eclinic.firebase

/**
 * Enum class representing the possible statuses of an appointment in the eClinic system.
 */
enum class AppointmentStatus {
    /** Appointment request has been made and is awaiting doctor's decision. */
    PENDING,
    /** Appointment has been accepted and scheduled. */
    CONFIRMED,
    /** Appointment has occurred. */
    COMPLETED,
    /** Appointment has been cancelled by either party. */
    CANCELLED
}

/**
 * Data class representing an appointment in the eClinic application.
 * This class is used for serializing and deserializing appointment data to and from Firebase Firestore.
 *
 * @property id The unique ID of the appointment document in Firestore.
 * @property doctorId The Firebase User ID (UID) of the doctor involved in the appointment.
 * @property patientId The Firebase User ID (UID) of the patient involved in the appointment.
 * @property dateTime A string representation of the date and time of the appointment.
 * Consider using a more specific format (e.g., ISO 8601) or a [java.util.Date] object for better handling.
 * @property status The current status of the appointment, using the [AppointmentStatus] enum.
 * @property prescriptionId Optional. The ID of the prescription issued during this appointment, if any.
 * @property chatId Optional. The ID of the chat conversation related to this appointment, if any.
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