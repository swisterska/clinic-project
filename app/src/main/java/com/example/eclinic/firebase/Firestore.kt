package com.example.eclinic.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * A class that handles Firestore operations using Kotlin coroutines.
 */
class Firestore {

    // Instance of FirebaseFirestore to interact with the Firestore database
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * Registers or updates a user in Firestore.
     *
     * @param user The user object to save.
     * @throws Exception If there is an error during the save operation.
     */
    suspend fun registerOrUpdateUser(user: User) {
        try {
            // Save or overwrite the user document with the given user ID
            mFireStore.collection("users")
                .document(user.id)
                .set(user)
                .await() // 🔹 Fix: Await Firestore operation to ensure it completes

            Log.d("Firestore", "User successfully registered/updated.")

        } catch (e: Exception) {
            Log.e("Firestore", "Error saving user data: ${e.message}", e)
            throw Exception("Error saving user data: ${e.message}")
        }
    }

    /**
     * Loads user data from Firestore.
     *
     * @param userId The ID of the user whose data is to be loaded.
     * @return A map containing user data, or null if the document does not exist.
     * @throws Exception If there is an error during the fetch operation.
     */
    suspend fun loadUserData(userId: String): Map<String, Any>? {
        try {
            val documentSnapshot = mFireStore.collection("users")
                .document(userId)
                .get()
                .await() // Suspends until the document is retrieved

            return documentSnapshot.data // Returns the document data as a map, or null if the document does not exist
        } catch (e: Exception) {
            Log.e("Firestore", "Error loading user data: ${e.message}", e)
            throw Exception("Error loading user data: ${e.message}")
        }
    }

    /**
     * Updates specific fields in a user's Firestore document.
     *
     * @param userId The ID of the user whose data is to be updated.
     * @param updatedData A map containing the fields to be updated.
     *                    Fields with `null` or blank string values will be ignored.
     * @throws Exception If there is an error during the update operation.
     */
    suspend fun updateUserData(userId: String, updatedData: Map<String, Any?>) {
        try {
            val filteredData = updatedData.filterValues { value ->
                value != null && !(value is String && value.isBlank())
            }

            if (filteredData.isEmpty()) return

            mFireStore.collection("users")
                .document(userId)
                .update(filteredData)
                .await() // Ensures Firestore operation completes before proceeding

            Log.d("Firestore", "User successfully updated.")

        } catch (e: Exception) {
            Log.e("Firestore", "Error updating user data: ${e.message}", e)
            throw Exception("Error updating user data: ${e.message}")
        }
    }
}
