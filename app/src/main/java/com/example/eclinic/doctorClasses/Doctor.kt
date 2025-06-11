package com.example.eclinic.doctorClasses

import com.google.firebase.firestore.DocumentId

data class Doctor(
    @DocumentId
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val specialization: String = "",
    val pwzNumber: String = "",
    val title: String = "",
    val workplace: String = "",
    val bio: String = "",
    val role: String = "doctor"
)
{
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}