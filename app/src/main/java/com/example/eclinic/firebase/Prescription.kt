package com.example.eclinic.firebase

/**
 * Data class representing a digital prescription issued by a doctor.
 * This class is used for serializing and deserializing prescription data to and from Firebase Firestore.
 *
 * @property doctorId The Firebase User ID (UID) of the doctor who issued this prescription.
 * @property patientId The Firebase User ID (UID) of the patient for whom this prescription was issued.
 * @property timestamp The [com.google.firebase.Timestamp] indicating when the prescription was issued.
 * This is crucial for chronological ordering and accurate time tracking. It can be null if not set.
 * @property url The URL to the digital PDF file of the prescription, typically stored in Firebase Storage.
 */
data class Prescription(
    val doctorId: String = "",
    val patientId: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val url: String = "",
)