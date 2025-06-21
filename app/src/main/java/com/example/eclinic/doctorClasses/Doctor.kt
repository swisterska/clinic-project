package com.example.eclinic.doctorClasses

import com.google.firebase.firestore.DocumentId

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
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}