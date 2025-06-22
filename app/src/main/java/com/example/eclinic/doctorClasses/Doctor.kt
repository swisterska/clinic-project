package com.example.eclinic.doctorClasses

import com.google.firebase.firestore.DocumentId

/**
 * Data class representing a Doctor user in the eClinic application.
 * This class is used for serializing and deserializing doctor data to and from Firebase Firestore.
 *
 * @property uid The unique Firebase User ID (UID) of the doctor.
 * This is automatically populated by Firestore's `@DocumentId` annotation
 * when retrieving documents where the document ID matches the UID.
 * @property firstName The first name of the doctor.
 * @property lastName The last name of the doctor.
 * @property email The email address of the doctor, used for authentication.
 * @property phone The phone number of the doctor.
 * @property specialization The medical specialization of the doctor (e.g., "Cardiologist", "Pediatrician").
 * @property pwzNumber The PWZ (Prawo Wykonywania Zawodu) number, which is a professional license number for doctors in Poland.
 * @property title The professional title of the doctor (e.g., "Dr.", "Prof.").
 * @property workplace The name of the workplace or clinic where the doctor practices.
 * @property bio A short biography or description provided by the doctor.
 * @property role The role of the user, which is "doctor" for instances of this class.
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for sending push notifications to the doctor's device.
 */
data class Doctor(
    @DocumentId
    var uid: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phone: String = "",
    var specialization: String = "",
    var pwzNumber: String = "",
    var title: String = "",
    var workplace: String = "",
    var bio: String = "",
    var role: String = "doctor",
    var fcmToken: String = ""
)

{
    /**
     * No-argument constructor for Firebase Firestore deserialization.
     * Initializes all properties with default empty string values.
     */
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}