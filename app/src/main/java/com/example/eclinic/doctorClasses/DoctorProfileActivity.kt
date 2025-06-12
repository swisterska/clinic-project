package com.example.eclinic.doctorClasses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.example.eclinic.patientClasses.PatientsListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorProfileActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var profileImageView: ImageView
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var specializationTextView: TextView
    private lateinit var pwzTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var workplaceTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_doc)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        profileImageView = findViewById(R.id.profile)
        fullNameTextView = findViewById(R.id.doctorFullName)
        emailTextView = findViewById(R.id.doctorEmail)
        phoneTextView = findViewById(R.id.doctorPhone)
        specializationTextView = findViewById(R.id.doctorSpecialization)
        pwzTextView = findViewById(R.id.doctorPwz)
        titleTextView = findViewById(R.id.doctorTitle)
        workplaceTextView = findViewById(R.id.doctorWorkplace)
        bioTextView = findViewById(R.id.doctorBio)
        editProfileButton = findViewById(R.id.editDoctorProfileButton)
        logoutButton = findViewById(R.id.logoutButton)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            val docRef = firestore.collection("users").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val title = document.getString("title") ?: ""
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""

                        val fullNameWithTitle = if (title.isNotEmpty()) {
                            "$title $firstName $lastName"
                        } else {
                            "$firstName $lastName"
                        }
                        fullNameTextView.text = fullNameWithTitle
                        emailTextView.text = document.getString("email") ?: ""
                        phoneTextView.text = document.getString("phone") ?: ""
                        specializationTextView.text = document.getString("specialization") ?: ""
                        pwzTextView.text = document.getString("pwz") ?: ""
                        workplaceTextView.text = document.getString("workplace") ?: ""
                        bioTextView.text = document.getString("bio") ?: ""

                    } else {
                        Log.d("Firestore", "No doctor document found for userId: $userId")
                        Toast.makeText(this, "Profile data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firestore", "Error fetching doctor data: ", exception)
                    Toast.makeText(this, "An error occurred while fetching profile data.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("Auth", "User ID is null. User might not be logged in.")
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditDoctorProfileActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, com.example.eclinic.logRegClasses.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
