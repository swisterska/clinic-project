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
 * Activity that allows a doctor user to change their account password.
 *
 * It displays the profile image and provides input fields for the old password,
 * new password, and confirmation of the new password.
 * Handles password validation and updates the password in Firebase Authentication.
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
     * Initializes views, loads the user's profile image from Firebase Storage,
     * and sets up the password change button click listener.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down, this Bundle contains the data it most recently supplied.
     * Otherwise, it is null.
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

        // Load profile image from Firebase Storage (example path used)
        val profilePictureRef = storage.reference.child("users/${currentUser?.uid}/profile.jpg")
        profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.applogo)
                .error(R.drawable.applogo)
                .into(profileImageView)
        }.addOnFailureListener {
            // Optional: Handle failure to load image if needed
        }

        // Set click listener to validate input and update password
        saveNewPasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()

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

            // Update password in Firebase Authentication
            currentUser?.updatePassword(newPassword)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password has been changed", Toast.LENGTH_SHORT).show()
                        finish() // Close this activity and return
                    } else {
                        Toast.makeText(this, "Failed to change password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
