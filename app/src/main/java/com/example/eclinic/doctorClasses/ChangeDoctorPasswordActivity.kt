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

class ChangeDoctorPasswordActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var saveNewPasswordButton: Button
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val storage = FirebaseStorage.getInstance()

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

        // Load profile image (you might need to adjust this based on how you store it)
        val profilePictureRef = storage.reference.child("users/${currentUser?.uid}/profile.jpg") // Example path
        profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.applogo)
                .error(R.drawable.applogo)
                .into(profileImageView)
        }.addOnFailureListener {
        }

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

            currentUser?.updatePassword(newPassword)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password has been changed", Toast.LENGTH_SHORT).show()
                        finish() // Go back to the edit profile screen
                    } else {
                        Toast.makeText(this, "Failed to change password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}