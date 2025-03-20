package com.example.eclinic.firebase

enum class Role {
    PATIENT,
    DOCTOR,
    ADMIN,
    UNDEFINED
}

data class User(
    var id: String = "",
    var email: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var role: Role = Role.UNDEFINED
)
