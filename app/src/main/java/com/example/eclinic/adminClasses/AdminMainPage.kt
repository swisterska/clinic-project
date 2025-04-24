package com.example.eclinic.adminClasses

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminMainPage : AppCompatActivity() {

    private lateinit var layoutContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main_page)

        layoutContainer = findViewById(R.id.layoutContainer)

        fetchUnverifiedDoctors()
    }

    private fun fetchUnverifiedDoctors() {
        layoutContainer.removeAllViews() // clear previous views

        db.collection("users")
            .whereEqualTo("role", "DOCTOR")
            .whereEqualTo("verified", false)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val emptyText = TextView(this).apply {
                        text = "No unverified doctors found."
                        textSize = 16f
                        gravity = Gravity.CENTER
                    }
                    layoutContainer.addView(emptyText)
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val doctorId = doc.id
                    val name = doc.getString("firstName") ?: "Unknown"
                    val surname = doc.getString("lastName") ?: ""
                    val specialization = doc.getString("specialization") ?: "Not Specified"

                    // Container layout for each doctor
                    val doctorLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16, 16, 16, 16)
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, 24)
                        }

                    }

                    val doctorInfo = TextView(this).apply {
                        text = "Name: $name $surname\nSpecialization: $specialization"
                        textSize = 16f
                    }

                    val verifyButton = Button(this).apply {
                        text = "Verify"
                        setOnClickListener { verifyDoctor(doctorId) }
                    }

                    doctorLayout.addView(doctorInfo)
                    doctorLayout.addView(verifyButton)
                    layoutContainer.addView(doctorLayout)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch unverified doctors.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verifyDoctor(doctorId: String) {
        db.collection("users")
            .document(doctorId)
            .update("verified", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Doctor verified successfully!", Toast.LENGTH_SHORT).show()
                fetchUnverifiedDoctors() // Refresh the list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Verification failed. Try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
