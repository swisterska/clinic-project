package com.example.eclinic.firebase

/**
 * Enum class representing the different roles a user can have in the eClinic system.
 * - [PATIENT]: A user who seeks medical services.
 * - [DOCTOR]: A user who provides medical services.
 * - [ADMIN]: A user with administrative privileges.
 * - [UNDEFINED]: A default or unassigned role.
 */
enum class Role {
    PATIENT,
    DOCTOR,
    ADMIN,
    UNDEFINED
}

/**
 * Data class representing a generic user profile in the eClinic system.
 * This class holds common details for all types of users, with some fields
 * being specific to certain roles (e.g., specialization for doctors).
 *
 * @property id The unique identifier of the user, typically corresponding to their Firebase Authentication UID.
 * @property email The email address of the user.
 * @property firstName The first name of the user.
 * @property lastName The last name of the user.
 * @property role The role of the user, defaulting to [Role.UNDEFINED].
 * @property phoneNumber The phone number of the user.
 * @property profilePictureUrl The URL to the user's profile picture.
 * @property dateOfBirth The date of birth of the user, typically in a string format (e.g., "YYYY-MM-DD").
 * @property specialization The medical specialization of the user, primarily relevant for [Role.DOCTOR] users.
 * @property verified A boolean indicating whether the user's account has been verified, defaulting to `true`.
 */
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
    * * Key Features of Companion Object:
    * * Singleton by Default: There is only one instance of a companion object per class.
    * * Access to Private Members: It can access the private members of the containing class.
    * * Implicit Name: If you don't name the companion object, it will automatically get the name Companion.
    * * Implements Interfaces: A companion object can implement interfaces.
    * * Extension Functions: You can define extension functions for a companion object.
    */
    companion object {
        /**
         * Converts a map of Firestore document data into a [User] object.
         *
         * This method ensures type safety by checking the type of each field in the map before
         * assigning it to the corresponding property in the `User` object. If a field is missing
         * or has the wrong type, a default value is used instead.
         *
         * @param data The map containing the user data fetched from Firestore.
         * @return A [User] object containing the mapped data.
         */
        fun fromMap(data: Map<String, Any?>): User {
            // Safely retrieve the "role" field and convert it to the Role enum, defaulting to UNDEFINED.
            val role = Role.valueOf(data["role"] as? String ?: "UNDEFINED")
            return User(
                // Retrieve the "id" field as a string, or use an empty string if it's missing or not a String.
                id = data["id"] as? String ?: "",

                // Retrieve the "firstName" field as a String, or use an empty string if missing.
                firstName = data["firstName"] as? String ?: "",

                // Retrieve the "lastName" field as a String, or use an empty string if missing.
                lastName = data["lastName"] as? String ?: "",

                // Retrieve the "email" field as a string, or use an empty string if it's missing or not a String.
                email = data["email"] as? String ?: "",

                // Retrieve the "phoneNumber" field as a string, or use an empty string if it's missing or not a String.
                phoneNumber = data["phoneNumber"] as? String ?: "",

                // Retrieve the "profilePictureUrl" field as a string, or use an empty string if it's missing or not a String.
                profilePictureUrl = data["profilePictureUrl"] as? String ?: "",

                // Retrieve the "dateOfBirth" field as a String, or use an empty string if missing.
                dateOfBirth = data["dateOfBirth"] as? String ?: "",

                // Retrieve the "verified" field as a Boolean, or default to true if missing or not a Boolean.
                verified = data["verified"] as? Boolean ?: true,

                // Retrieve "specialization" only if the role is DOCTOR, otherwise it's an empty string.
                specialization = if (role == Role.DOCTOR) {
                    data["specialization"] as? String ?: ""
                } else ""
            ).apply {
                // Assign the determined role to the User object.
                this.role = role
            }
        }
    }
}