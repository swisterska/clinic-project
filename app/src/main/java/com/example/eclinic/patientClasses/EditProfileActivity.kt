package com.example.eclinic.patientClasses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.example.eclinic.logRegClasses.LoginActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * [EditProfileActivity] allows authenticated users (patients) to view and update their profile information.
 * Users can modify their name, surname, birth date, email, phone number, and also change their password.
 * Changes are saved to Firebase Firestore and Firebase Authentication.
 */
class EditProfileActivity : AppCompatActivity() {

    // UI elements for profile editing
    private lateinit var goBackButton: ImageButton
    private lateinit var editName: EditText
    private lateinit var editSurname: EditText
    private lateinit var editBirthDate: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editOldPassword: EditText
    private lateinit var editNewPassword: EditText
    private lateinit var editConfirmNewPassword: EditText
    private lateinit var btnConfirm: androidx.appcompat.widget.AppCompatButton

    // Firebase instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser // Current authenticated Firebase user

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up click listeners for navigation, date picker,
     * and profile update actions. Loads the current user's profile data.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize UI elements
        goBackButton = findViewById(R.id.GoBackButton)
        editName = findViewById(R.id.editName)
        editSurname = findViewById(R.id.editSurname)
        editBirthDate = findViewById(R.id.editBirthDate)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        editOldPassword = findViewById(R.id.editOldPassword)
        editNewPassword = findViewById(R.id.editNewPassword)
        editConfirmNewPassword = findViewById(R.id.editConfirmNewPassword)
        btnConfirm = findViewById(R.id.btnConfirm)

        // Set up click listener for the back button to finish the activity
        goBackButton.setOnClickListener {
            finish()
        }

        // Set up click listener for the birth date EditText to show a DatePickerDialog
        editBirthDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Load the current user's profile data into the EditText fields
        loadUserProfile()

        // Set up click listener for the confirm button to update the user's profile
        btnConfirm.setOnClickListener {
            updateUserProfile()
        }
    }

    /**
     * Loads the current user's profile data from Firebase Firestore and populates the
     * corresponding EditText fields. It fetches the data from the "users" collection
     * using the current user's UID.
     */
    private fun loadUserProfile() {
        val userId = currentUser?.uid ?: return // Get current user's UID; return if null
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Populate EditText fields with data from Firestore document
                    editName.setText(document.getString("firstName") ?: "")
                    editSurname.setText(document.getString("lastName") ?: "")
                    editBirthDate.setText(document.getString("dateOfBirth") ?: "")
                    editEmail.setText(document.getString("email") ?: currentUser?.email ?: "") // Prioritize Firestore email, fallback to Auth email
                    editPhone.setText(document.getString("phoneNumber") ?: "")
                }
            }
            .addOnFailureListener {
                // Show a toast message if loading profile data fails
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handles the process of updating the user's profile.
     * It collects data from EditText fields, prepares updates for Firestore,
     * and handles email and password changes separately due to Firebase Authentication requirements.
     * If only profile details are updated, it logs out the user and redirects to LoginActivity.
     */
    private fun updateUserProfile() {
        val userId = currentUser?.uid ?: return // Get current user's UID; return if null
        val userDocument = firestore.collection("users").document(userId) // Reference to user's Firestore document

        // Get trimmed text from all editable fields
        val name = editName.text.toString().trim()
        val surname = editSurname.text.toString().trim()
        val birthDate = editBirthDate.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val phone = editPhone.text.toString().trim()

        val updates = hashMapOf<String, Any>() // HashMap to store updates for Firestore

        // Add non-empty updated fields to the updates map
        if (name.isNotEmpty()) {
            updates["firstName"] = name
        }
        if (surname.isNotEmpty()) {
            updates["lastName"] = surname
        }
        if (birthDate.isNotEmpty()) {
            updates["dateOfBirth"] = birthDate
        }
        if (phone.isNotEmpty()) {
            updates["phoneNumber"] = phone
        }

        // Handle email update separately as it requires FirebaseAuth.updateEmail()
        if (email.isNotEmpty() && email != currentUser?.email) {
            currentUser?.updateEmail(email)
                ?.addOnSuccessListener {
                    updates["email"] = email // Add email to Firestore updates after successful Auth update
                    updateFirestore(userDocument, updates) // Update Firestore with all changes
                    Toast.makeText(this, "Email updated. You might need to re-login.", Toast.LENGTH_LONG).show()
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_LONG).show()
                }
            return // Exit function here to prevent immediate Firestore update if email is being changed
        }

        // If no email change, proceed with Firestore update if there are any other changes
        if (updates.isNotEmpty()) {
            updateFirestore(userDocument, updates)
        } else {
            // If no profile changes were made (and no email update happened), just log out and go to login
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Always attempt to update password regardless of other profile changes
        updatePassword()
    }

    /**
     * Updates the user's profile document in Firebase Firestore with the provided [updates].
     * On successful update, it displays a toast message, logs out the user, and redirects to [LoginActivity].
     * On failure, it displays an error toast message.
     * @param userDocument A [com.google.firebase.firestore.DocumentReference] pointing to the user's document.
     * @param updates A [HashMap] containing the fields and their new values to be updated in Firestore.
     */
    private fun updateFirestore(userDocument: com.google.firebase.firestore.DocumentReference, updates: HashMap<String, Any>) {
        userDocument.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                auth.signOut() // Log out after successful update
                startActivity(Intent(this, LoginActivity::class.java)) // Redirect to login page
                finish() // Finish this activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Handles the process of updating the user's password.
     * It re-authenticates the user with their old password before attempting to set a new one.
     * Displays success or failure messages via toasts.
     */
    private fun updatePassword() {
        val oldPassword = editOldPassword.text.toString()
        val newPassword = editNewPassword.text.toString()
        val confirmNewPassword = editConfirmNewPassword.text.toString()

        // Check if all password fields are filled and new passwords match
        if (oldPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmNewPassword) {
            // Re-authenticate user with old password
            currentUser?.reauthenticate(EmailAuthProvider.getCredential(currentUser.email!!, oldPassword))
                ?.addOnSuccessListener {
                    // If re-authentication successful, update password
                    currentUser.updatePassword(newPassword)
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            // Clear password fields after successful update
                            editOldPassword.text.clear()
                            editNewPassword.text.clear()
                            editConfirmNewPassword.text.clear()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                ?.addOnFailureListener { e ->
                    // Handle re-authentication failure (e.g., incorrect old password)
                    Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else if (oldPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmNewPassword.isNotEmpty()) {
            // Warn user if password fields are partially filled or new passwords don't match
            Toast.makeText(this, "Please fill all password fields correctly to update password", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Displays a [DatePickerDialog] for the user to select their birth date.
     * The selected date is formatted as "yyyy-MM-dd" and set as the text of `editBirthDate`.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editBirthDate.setText(dateFormat.format(selectedDate.time))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}