package com.example.eclinic.patientClasses

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageButton
import android.widget.TextView
import com.example.eclinic.R
import com.example.eclinic.logRegClasses.LogRegActivity

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainPagePatient : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page_patient)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        welcomeText = findViewById(R.id.tvWelcomeMessage)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        welcomeText.text = "Welcome, $firstName"
                    }
                }
                .addOnFailureListener {
                    welcomeText.text = "Welcome, User"
                }

        }


        val appointmentRegister = findViewById<ImageButton>(R.id.makeAnAppointment)
        appointmentRegister.setOnClickListener {
            val intent = Intent(this, RegisterForAppointmentPatient::class.java)
            startActivity(intent)
        }




        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatPatientActivity::class.java))
                    true
                }
                R.id.nav_prescriptions -> {
                    startActivity(Intent(this, PrescriptionsActivity::class.java))
                    true
                }
                R.id.nav_appointments -> {
                    startActivity(Intent(this, AppointmentsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

}