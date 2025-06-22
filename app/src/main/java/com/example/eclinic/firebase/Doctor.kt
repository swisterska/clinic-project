package com.example.eclinic.firebase

/**
 * Data class representing a Doctor's core information, likely for display or quick lookup.
 * This class is used for serializing and deserializing doctor data to and from Firebase Firestore.
 * Note: This class appears to be a simplified representation compared to `com.example.eclinic.doctorClasses.Doctor`.
 *
 * @property id The unique ID of the doctor, typically their Firebase User ID (UID).
 * @property name The full name of the doctor.
 * @property bio A short biography or general description of the doctor.
 * @property description A more detailed description, possibly including specialties or approach.
 * @property profilePicUrl The URL to the doctor's profile picture stored in Firebase Storage.
 * @property title The professional title of the doctor (e.g., "Dr.", "Prof.").
 * @property workplace The name of the workplace or clinic where the doctor practices.
 * @property pwz The PWZ (Prawo Wykonywania Zawodu) number, a professional license number for doctors in Poland.
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for sending push notifications to the doctor's device.
 */
data class Doctor(
    var id: String = "",
    var name: String = "",
    var bio: String = "",
    var description: String = "",
    var profilePicUrl: String = "",
    var title: String = "",
    var workplace: String = "",
    var pwz: String = "",
    var fcmToken: String = ""
) {
    // No custom constructor or methods are explicitly defined in the provided snippet beyond the default data class features.
}