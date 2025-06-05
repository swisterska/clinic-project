package com.example.eclinic.doctorClasses

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.TextView
import com.example.eclinic.R
import com.example.eclinic.calendar.MainCalendarActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


import com.google.android.material.bottomnavigation.BottomNavigationView

class MainPageDoctor : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page_doctor)

        val bottomNavDoc = findViewById<BottomNavigationView>(R.id.bottom_navigation_doctor)

        welcomeText = findViewById(R.id.tvWelcomeMessage)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        welcomeText.text = "Welcome, dr. $firstName"
                    }
                }
                .addOnFailureListener {
                    welcomeText.text = "Welcome, Doctor"
                }

        }



        bottomNavDoc.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ChatDocActivity::class.java))
                    true
                }
                R.id.navigation_prescriptions_doc -> {
                    startActivity(Intent(this, PrescriptionsDocActivity::class.java))
                    true
                }
                R.id.navigation_calendar -> {
                    startActivity(Intent(this, MainCalendarActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileDocActivity::class.java))
                    true
                }
                else -> false
            }
        }
}}