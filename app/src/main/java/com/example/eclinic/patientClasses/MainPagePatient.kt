package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.example.eclinic.chat.ChatPatientActivity
import com.example.eclinic.logRegClasses.LogRegActivity
import com.example.eclinic.logRegClasses.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainPagePatient : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var scheduleAnApointment: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page_patient)

        welcomeText = findViewById(R.id.petNameTextView)
        bottomNav = findViewById(R.id.bottom_navigation)
        scheduleAnApointment = findViewById(R.id.ScheduleAnApointmentBtn)

        scheduleAnApointment.setOnClickListener {
            val intent = Intent(this, RegisterForAppointmentPatient::class.java)
            startActivity(intent)
        }



        // Pobierz dane użytkownika z Firestore
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        welcomeText.text = "Welcome, $firstName"
                    }
                }
                .addOnFailureListener {
                    welcomeText.text = "Welcome, User"
                }
        } else {
            welcomeText.text = "Welcome, Guest"
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
