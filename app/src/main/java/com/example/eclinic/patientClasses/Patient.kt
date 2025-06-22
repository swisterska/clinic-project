package com.example.eclinic.patientClasses

import com.google.firebase.firestore.DocumentId

/**
 * Data class representing a Patient user in the eClinic application.
 * This class maps to a document in the "users" collection in Firebase Firestore,
 * specifically for documents where the 'role' field is "patient".
 *
 * @property uid The unique identifier for the patient, annotated with [DocumentId] to map directly
 * from the Firestore document ID.
 * @property firstName The first name of the patient. Defaults to an empty string.
 * @property lastName The last name of the patient. Defaults to an empty string.
 * @property email The email address of the patient. Defaults to an empty string.
 * @property phone The phone number of the patient. Defaults to an empty string. (Note: there is also a 'phoneNumber' field)
 * @property dateOfBirth The patient's date of birth in string format (e.g., "YYYY-MM-DD"). Defaults to an empty string.
 * @property gender The gender of the patient. Defaults to an empty string.
 * @property role The role of the user, which is "patient" for this class. Defaults to "patient".
 * @property phoneNumber The phone number of the patient. Defaults to an empty string. This field seems to duplicate 'phone'.
 * @property profilePictureUrl The URL to the patient's profile picture. Defaults to an empty string.
 * @property id An additional ID field, purpose might vary (could be internal or external ID). Defaults to an empty string.
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for sending notifications to the patient's device. Defaults to an empty string.
 */
data class Patient(
    @DocumentId
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val role: String = "patient",
    val phoneNumber: String = "",            // dodane
    val profilePictureUrl: String = "",      // dodane
    val id: String = "",
    val fcmToken: String = ""
)