package com.example.eclinic.adminClasses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.doctorClasses.Doctor
import com.example.eclinic.patientClasses.DoctorAdapter
import com.google.firebase.firestore.FirebaseFirestore

class AdminCalendarActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter

    private val doctorList = mutableListOf<Doctor>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_calendar)

        recyclerView = findViewById(R.id.recyclerViewCalendar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        doctorAdapter = DoctorAdapter(
            doctorList,
            onDoctorClick = { /* no action for now */ },
            onInfoClick = { /* no action, hidden anyway */ }
        )
        recyclerView.adapter = doctorAdapter

        fetchDoctors()
    }

    private fun fetchDoctors() {
        db.collection("users")
            .whereEqualTo("role", "DOCTOR")
            .whereEqualTo("verified", true)
            .get()
            .addOnSuccessListener { documents ->
                doctorList.clear()
                for (doc in documents) {
                    val doctor = doc.toObject(Doctor::class.java)
                    doctorList.add(doctor)
                }
                doctorAdapter.notifyDataSetChanged()
            }
    }
}
