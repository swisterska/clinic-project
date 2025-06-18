package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.calendar.MainCalendarActivity
import com.example.eclinic.firebase.Visit
import com.google.firebase.firestore.FirebaseFirestore

class VisitTypeActivity : AppCompatActivity() {

    private lateinit var visitTypesTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var noVisitsTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    private lateinit var doctorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_type)

        visitTypesTextView = findViewById(R.id.visit_title)
        noVisitsTextView = findViewById(R.id.no_visits_text)

        doctorId = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Brak ID lekarza", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchVisitTypesFromFirestore()
    }

    private fun fetchVisitTypesFromFirestore() {
        db.collection("visitTypes").document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                val visitList = mutableListOf<Visit>()
                val data = document.data

                if (data != null && data.isNotEmpty()) {
                    for ((name, price) in data) {
                        visitList.add(Visit(name, price.toString()))
                    }

                    setupRecyclerView(visitList)
                    noVisitsTextView.visibility = TextView.GONE
                } else {
                    noVisitsTextView.visibility = TextView.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load visit types: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                noVisitsTextView.text = "Failed to load."
                noVisitsTextView.visibility = View.VISIBLE
            }
    }

    private fun setupRecyclerView(visits: List<Visit>) {
        val recyclerView = findViewById<RecyclerView>(R.id.visit_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = VisitAdapter(visits) { selectedVisit ->
            val intent = Intent(this, MainCalendarActivity::class.java)
            intent.putExtra("id", doctorId)
            intent.putExtra("visitName", selectedVisit.name)
            intent.putExtra("visitPrice", selectedVisit.price)
            startActivity(intent)
        }
    }
}
