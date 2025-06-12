package com.example.eclinic.calendar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WeeklyScheduleActivityPatient : AppCompatActivity() {

    private lateinit var dateTitle: TextView
    private lateinit var btnConfirmVisit: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var doctorId: String
    private lateinit var selectedDate: Calendar
    private var selectedSlots: List<String> = listOf()
    private var selectedPatientSlot: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_schedule)

        doctorId = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<Button>(R.id.btnAddSlots)?.visibility = View.GONE


        val selectedDateMillis = intent.getLongExtra("selectedDate", 0L)
        selectedDate = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

        dateTitle = findViewById(R.id.dateTitle)
        btnConfirmVisit = findViewById(R.id.btnConfirmVisit)
        recyclerView = findViewById(R.id.recyclerView)

        btnConfirmVisit.visibility = Button.GONE
        btnConfirmVisit.setOnClickListener {
            selectedPatientSlot?.let { bookAppointment(it) }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        updateDateTitle()
        loadSlotsFromFirebase()
    }

    private fun updateDateTitle() {
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        dateTitle.text = formatter.format(selectedDate.time)
    }

    private fun updateTimeSlots() {
        val adapter = TimeSlotDisplayAdapter(
            selectedSlots,
            selectedSlot = selectedPatientSlot,
            onSlotSelected = { selectedTime ->
                selectedPatientSlot = selectedTime
                btnConfirmVisit.visibility = Button.VISIBLE
                updateTimeSlots()
            }
        )
        recyclerView.adapter = adapter
    }


    private fun loadSlotsFromFirebase() {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        db.collection("appointments").document(doctorId).get().addOnSuccessListener { doc ->
            val allSlots = (doc[dateKey] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            // Also get booked slots
            db.collection("confirmedAppointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", dateKey)
                .get()
                .addOnSuccessListener { confirmedDocs ->
                    val bookedSlots = confirmedDocs.mapNotNull { it.getString("hour") }

                    // Remove booked slots from available slots
                    selectedSlots = allSlots.filter { it !in bookedSlots }

                    updateTimeSlots()
                }
        }
    }


    private fun bookAppointment(slot: String) {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val visitName = intent.getStringExtra("visitName") ?: "Unknown"

        val appointment = mapOf(
            "patientId" to patientId,
            "doctorId" to doctorId,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time),
            "hour" to slot,
            "timestamp" to System.currentTimeMillis(),
            "typeOfTheVisit" to visitName

        )

        db.collection("confirmedAppointments")
            .add(appointment)
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Booking failed.", Toast.LENGTH_SHORT).show()
            }
    }

}
