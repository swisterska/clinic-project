package com.example.eclinic.adminClasses

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.calendar.MainCalendarActivity
import com.example.eclinic.doctorClasses.Doctor
import com.example.eclinic.patientClasses.DoctorAdapter
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for administrators to view the calendar of verified doctors.
 * Displays a list of verified doctors and allows opening individual calendars.
 */
class AdminCalendarActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter

    private val doctorList = mutableListOf<Doctor>()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Called when the activity is starting.
     * Sets up the UI components and fetches the list of doctors.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_calendar)

        recyclerView = findViewById(R.id.recyclerViewCalendar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        doctorAdapter = DoctorAdapter(
            doctorList,
            onDoctorClick = { doctor -> openDoctorCalendar(doctor) },
            onInfoClick = { /* no action, hidden anyway */ }
        )
        recyclerView.adapter = doctorAdapter

        fetchDoctors()
    }

    /**
     * Fetches the list of verified doctors from Firestore and updates the RecyclerView.
     */
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

    /**
     * Opens the selected doctor's calendar.
     *
     * @param doctor The doctor whose calendar should be opened.
     */
    private fun openDoctorCalendar(doctor: Doctor) {
        doctor.uid?.let { uid ->
            val intent = Intent(this, MainCalendarActivity::class.java)
            intent.putExtra("id", uid)
            startActivity(intent)
        } ?: run {
            Toast.makeText(this, "Invalid doctor ID", Toast.LENGTH_SHORT).show()
        }
    }

}
