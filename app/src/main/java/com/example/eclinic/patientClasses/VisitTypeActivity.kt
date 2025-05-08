package com.example.eclinic.patientClasses

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eclinic.R
import com.google.firebase.firestore.FirebaseFirestore

class VisitTypeActivity : AppCompatActivity() {

    private lateinit var visitTypesTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_type)

        visitTypesTextView = findViewById(R.id.visit_types_text_view)

        val specialization = intent.getStringExtra("specialization")

        if (specialization != null) {
            loadVisitTypesForSpecialization(specialization)
        } else {
            visitTypesTextView.text = "No specialization found"
        }
    }

    private fun loadVisitTypesForSpecialization(specialization: String) {
        db.collection("visitTypes")
            .whereEqualTo("specialization", specialization)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    visitTypesTextView.text = "No visit types available for $specialization"
                } else {
                    val visitTypes = documents.map { it.getString("type") ?: "Unknown" }
                    visitTypesTextView.text = "Visit Types for $specialization:\n" + visitTypes.joinToString("\n")
                }
            }
            .addOnFailureListener {
                visitTypesTextView.text = "Error loading visit types"
            }
    }
}
