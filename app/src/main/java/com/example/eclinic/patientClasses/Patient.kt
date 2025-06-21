package com.example.eclinic.patientClasses

import com.google.firebase.firestore.DocumentId

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
    val fcmToken: String = "")