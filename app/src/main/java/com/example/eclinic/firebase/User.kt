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
    var role: Role = Role.UNDEFINED,
    var phoneNumber: String = "",
    var profilePictureUrl: String = "",
    var dateOfBirth: String = "",
    var specialization: String = "",
    var verified: Boolean = true


) {
    /*
    *  * Key Features of Companion Object:
    *          * Singleton by Default: There is only one instance of a companion object per class.
    *          * Access to Private Members: It can access the private members of the containing class.
    *          * Implicit Name: If you don't name the companion object, it will automatically get the name Companion.
    *          * Implements Interfaces: A companion object can implement interfaces.
    *          * Extension Functions: You can define extension functions for a companion object.
    */
    companion object {
        /**
         * Converts a map of Firestore document data into a User object.
         *
         * This method ensures type safety by checking the type of each field in the map before
         * assigning it to the corresponding property in the `User` object. If a field is missing
         * or has the wrong type, a default value is used instead.
         *
         * @param data The map containing the user data fetched from Firestore.
         * @return A `User` object containing the mapped data.
         *
         */
        fun fromMap(data: Map<String, Any?>): User {
            val role = Role.valueOf(data["role"] as? String ?: "UNDEFINED")
            return User(
                // Retrieve the "id" field as a string, or use an empty string if it's missing
                id = data["id"] as? String ?: "",

                // Retrieve the "name" field as a String
                firstName = data["firstName"] as? String ?: "",

                lastName = data["lastName"] as? String ?: "",

                // Retrieve the "email" field as a string, or use an empty string if it's missing
                email = data["email"] as? String ?: "",

                // Retrieve the "phoneNumber" field as a string, or use an empty string if it's missing
                phoneNumber = data["phoneNumber"] as? String ?: "",

                // Retrieve the "profilePictureUrl" field as a string, or use an empty string if it's missing
                profilePictureUrl = data["profilePictureUrl"] as? String ?: "",

                verified = data["verified"] as? Boolean ?: true,

                specialization = if (role == Role.DOCTOR) {
                    data["specialization"] as? String ?: ""
                }
                else "")
    }
}
}
