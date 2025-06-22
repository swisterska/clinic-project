package com.example.eclinic.doctorClasses

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage

/**
 * Activity for doctors to change their password within the eClinic application.
 * Users are required to enter their old password and then set and confirm a new password.
 * The activity also displays the doctor's profile image.
 */
class ChangeDoctorPasswordActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var saveNewPasswordButton: Button
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val storage = FirebaseStorage.getInstance()

    /**
     * Called when the activity is first created.
     * Initializes UI elements, sets up Firebase authentication, loads the user's profile image,
     * and configures the click listener for the save password button.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_doctor_password)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        profileImageView = findViewById(R.id.profile)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText)
        saveNewPasswordButton = findViewById(R.id.saveNewPasswordButton)

        // Load profile image from Firebase Storage
        // The path assumes profile images are stored under "users/{UID}/profile.jpg"
        val profilePictureRef = storage.reference.child("users/${currentUser?.uid}/profile.jpg") // Example path
        profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
            // Use Glide library to load the image into the ImageView
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.applogo) // Placeholder image while loading
                .error(R.drawable.applogo)      // Image to show if loading fails
                .into(profileImageView)
        }.addOnFailureListener {
            // Handle any errors during image download (e.g., file not found)
            // No specific action needed here other than the error placeholder being shown
        }

        saveNewPasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()

            // Validate input fields
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to update the user's password in Firebase Authentication
            currentUser?.updatePassword(newPassword)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password has been changed", Toast.LENGTH_SHORT).show()
                        finish() // Close this activity and go back to the previous screen (e.g., edit profile)
                    } else {
                        // Handle failure: e.g., old password not matching, weak new password, etc.
                        Toast.makeText(this, "Failed to change password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}