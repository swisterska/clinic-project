package com.example.eclinic.firebase

/**
 * Data class representing a Patient user in the eClinic application.
 * This class extends basic user information with patient-specific details.
 * It is used for serializing and deserializing patient data to and from Firebase Firestore.
 *
 * @property id The unique ID of the patient, typically their Firebase User ID (UID).
 * @property user An embedded [User] object containing common user details like first name, last name, email.
 * @property dateOfBirth The patient's date of birth, typically in a string format (e.g., "YYYY-MM-DD").
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for sending push notifications to the patient's device.
 * @property medicalHistory A list of strings representing the patient's medical history or significant conditions.
 * @property profilePictureUrl The URL to the patient's profile picture stored in Firebase Storage.
 * @property phoneNumber The patient's phone number.
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