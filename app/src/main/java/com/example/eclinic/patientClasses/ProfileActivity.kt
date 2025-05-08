package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eclinic.R
import com.example.eclinic.logRegClasses.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var profile: ImageView
    private lateinit var usernameText: TextView
    private lateinit var nameText: TextView
    private lateinit var surnameText: TextView
    private lateinit var birthDateText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var editProfileButton: AppCompatButton
    private lateinit var logoutButton: AppCompatButton

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        profile = findViewById(R.id.profile)
        usernameText = findViewById(R.id.profileUsername)
        nameText = findViewById(R.id.profileName)
        surnameText = findViewById(R.id.profileSurname)
        birthDateText = findViewById(R.id.profileBirthDate)
        emailText = findViewById(R.id.profileEmail)
        phoneText = findViewById(R.id.profilePhone)
        editProfileButton = findViewById(R.id.editProfileButton)
        logoutButton = findViewById(R.id.logoutButton)

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val birthDate = document.getString("dateOfBirth") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phoneNumber") ?: ""
                    val fullName = "$firstName $lastName"

                    usernameText.text = fullName
                    nameText.text = firstName
                    surnameText.text = lastName
                    birthDateText.text = birthDate
                    emailText.text = email
                    phoneText.text = phone
                } else {
                    Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error while fetching profile data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}