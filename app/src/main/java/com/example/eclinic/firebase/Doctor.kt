package com.example.eclinic.firebase

/**
 * Data class representing a Doctor user profile in the eClinic system.
 * This class holds various details about a doctor, including personal information,
 * professional background, and technical identifiers for push notifications.
 *
 * @property id The unique identifier of the doctor, typically corresponding to their Firebase Authentication UID.
 * @property name The full name of the doctor.
 * @property bio A short biography or summary of the doctor's experience.
 * @property description A more detailed description of the doctor's specialization, services, etc.
 * @property profilePicUrl The URL to the doctor's profile picture stored in Firebase Storage or similar.
 * @property title The professional title of the doctor (e.g., "Dr.", "Prof.").
 * @property workplace The name of the clinic, hospital, or institution where the doctor practices.
 * @property pwz The Polish Medical License Number (Prawo Wykonywania Zawodu).
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for sending push notifications to this doctor's device.
 */
data class Doctor(
    var id: String= "",
    var name: String = "",
    var bio: String = "",
    var description: String = "",
    var profilePicUrl: String = "",
    var title: String = "",
    var workplace: String = "",
    var pwz: String = "",
    var fcmToken: String = ""
) {

}