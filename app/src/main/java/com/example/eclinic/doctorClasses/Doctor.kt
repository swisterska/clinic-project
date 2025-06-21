package com.example.eclinic.doctorClasses

import com.google.firebase.firestore.DocumentId

/**
 * Data class representing a Doctor user in the eClinic app.
 *
 * @property uid Unique identifier of the doctor (Firestore document ID).
 * @property firstName Doctor's first name.
 * @property lastName Doctor's last name.
 * @property email Doctor's email address.
 * @property phone Doctor's phone number.
 * @property specialization Doctor's medical specialization.
 * @property pwzNumber Doctor's professional license number (PWZ).
 * @property title Doctor's academic or professional title (e.g., MD, PhD).
 * @property workplace The workplace or medical facility where the doctor practices.
 * @property bio Short biography or description of the doctor.
 * @property role User role in the system, default is "doctor".
 * @property fcmToken Firebase Cloud Messaging token for push notifications.
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
) {
    /**
     * No-argument constructor required by Firestore for deserialization.
     */
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}
