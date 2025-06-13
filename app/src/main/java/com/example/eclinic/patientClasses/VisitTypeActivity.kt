package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.calendar.MainCalendarActivity
import com.example.eclinic.calendar.WeeklyScheduleActivityPatient
import com.example.eclinic.firebase.Specialization
import com.example.eclinic.firebase.Visit
import com.example.eclinic.firebase.visitsBySpecialization
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class VisitTypeActivity : AppCompatActivity() {

    private lateinit var visitTypesTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    // ✅ Move doctorId here
    private lateinit var doctorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_type)

        val specializationName = intent.getStringExtra("specialization") ?: return
        val doctorName = intent.getStringExtra("doctorName")
        doctorId = intent.getStringExtra("id") ?: return

        val specialization = Specialization.fromString(specializationName)

        if (specialization != null) {
            val visits = visitsBySpecialization[specialization]
            if (visits != null) {
                setupRecyclerView(visits)
            }
        }
    }

    private fun setupRecyclerView(visits: List<Visit>) {
        val recyclerView = findViewById<RecyclerView>(R.id.visit_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = VisitAdapter(visits) { selectedVisit ->
            val intent = Intent(this, MainCalendarActivity::class.java)
            intent.putExtra("id", doctorId)
            intent.putExtra("visitName", selectedVisit.name)
            startActivity(intent)
        }
    }
}
