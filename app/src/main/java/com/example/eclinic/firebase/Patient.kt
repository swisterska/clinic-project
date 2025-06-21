package com.example.eclinic.firebase

/**
 * Data class representing a Patient user profile in the eClinic system.
 * This class extends the basic [User] information with patient-specific details,
 * including medical history and contact information.
 *
 * @property id The unique identifier of the patient, typically corresponding to their Firebase Authentication UID.
 * @property user A [User] object that contains common user details like name, email, etc.
 * @property dateOfBirth The patient's date of birth, typically in a string format (e.g., "YYYY-MM-DD").
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for sending push notifications to this patient's device.
 * @property medicalHistory A [List] of [String]s detailing the patient's medical history.
 * @property profilePictureUrl The URL to the patient's profile picture stored in Firebase Storage or similar.
 * @property phoneNumber The patient's contact phone number.
 */
data class Patient(
    var id: String = "",
    var user: User, // Links to a User object
    var dateOfBirth: String = "",
    val fcmToken: String = "",
    var medicalHistory: List<String> = emptyList(),
    var profilePictureUrl: String = "",
    var phoneNumber: String = ""

)