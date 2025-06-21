package com.example.eclinic.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * A class that handles Firestore operations using Kotlin coroutines.
 * This class provides suspend functions for interacting with the Firestore database,
 * making it suitable for use in coroutines without blocking the main thread.
 */
class Firestore {

    // Instance of FirebaseFirestore to interact with the Firestore database
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * Registers a new user or updates an existing user's data in Firestore.
     * If a document with the user's ID already exists, it will be overwritten with the new data.
     * This operation is performed asynchronously and the coroutine will suspend until it completes.
     *
     * @param user The [User] object containing the data to be saved or updated.
     * @throws Exception If an error occurs during the Firestore save operation, an [Exception] is thrown.
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
     * Loads a user's data from Firestore.
     * This operation fetches the document corresponding to the given user ID.
     *
     * @param userId The unique identifier of the user whose data is to be loaded.
     * @return A [Map] where keys are field names and values are their corresponding data,
     * or `null` if the user document does not exist in Firestore.
     * @throws Exception If an error occurs during the Firestore fetch operation, an [Exception] is thrown.
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
     * Updates specific fields in an existing user's Firestore document.
     * Only fields present in the [updatedData] map will be modified. Fields with `null`
     * or blank string values are intentionally ignored to prevent overwriting existing valid data
     * with empty values.
     *
     * @param userId The unique identifier of the user whose data is to be updated.
     * @param updatedData A [Map] containing the fields to be updated. The keys are the field names
     * and the values are the new data.
     * @throws Exception If an error occurs during the Firestore update operation, an [Exception] is thrown.
     */
    suspend fun updateUserData(userId: String, updatedData: Map<String, Any?>) {
        try {
            // Filter out null or blank string values to avoid overwriting valid data
            val filteredData = updatedData.filterValues { value ->
                value != null && !(value is String && value.isBlank())
            }

            // If no valid data is left after filtering, exit without performing an update
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