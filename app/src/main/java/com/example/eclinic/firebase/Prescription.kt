package com.example.eclinic.firebase

/**
 * Data class representing a prescription issued by a doctor.
 * This class stores key details about a prescription, including who issued it,
 * for whom it was issued, when, and where the actual prescription document is stored.
 *
 * @property doctorId The unique identifier of the doctor who issued this prescription.
 * @property patientId The unique identifier of the patient for whom this prescription was issued.
 * @property timestamp The [com.google.firebase.Timestamp] indicating when the prescription was issued.
 * This can be null if not yet set or retrieved.
 * @property url The URL pointing to the digital document of the prescription (e.g., a PDF stored in Firebase Storage).
 */
data class Prescription(
    val doctorId: String = "",
    val patientId: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val url: String = "",
)