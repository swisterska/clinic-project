package com.example.eclinic.calendar

import android.R.attr.id
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore


class WeeklyScheduleActivityDoctor : AppCompatActivity(), TimeSlotDialog.TimeSlotListener {

    private lateinit var dateTitle: TextView
    private lateinit var btnAddSlots: Button
    private lateinit var recyclerView: RecyclerView


    private lateinit var doctorId: String
    private lateinit var selectedDate: Calendar
    private var selectedSlots: List<String> = listOf()
    private lateinit var btnPrevDay: Button
    private lateinit var btnNextDay: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_schedule)

        doctorId = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dateTitle = findViewById(R.id.dateTitle)
        btnAddSlots = findViewById(R.id.btnAddSlots)
        recyclerView = findViewById(R.id.recyclerView)

        val selectedDateMillis = intent.getLongExtra("selectedDate", 0L)
        selectedDate = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

        updateDateTitle()

        btnAddSlots.setOnClickListener {
            val dialog = TimeSlotDialog.newInstance(selectedSlots)
            dialog.show(supportFragmentManager, "TimeSlotDialog")
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        updateTimeSlots()

        selectedDate.timeInMillis = selectedDateMillis
        updateDateTitle()

        btnPrevDay = findViewById(R.id.btnPrevDay)
        btnNextDay = findViewById(R.id.btnNextDay)

        btnPrevDay.setOnClickListener {
            changeDay(-1)
        }

        btnNextDay.setOnClickListener {
            changeDay(1)
        }
        loadSlotsFromFirebase()

    }


    private fun updateDateTitle() {
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        dateTitle.text = formatter.format(selectedDate.time)
    }

    private fun updateTimeSlots() {
        val adapter = TimeSlotDisplayAdapter(
            selectedSlots = selectedSlots,
            bookedSlots = bookedSlots,
            onBookedSlotClick = { bookedHour ->
                showBookedSlotInfo(bookedHour) // Doctor clicks red slot
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onSlotsSelected(slots: List<String>) {
        selectedSlots = slots
        updateTimeSlots()
        saveSlotsToFirebase()
    }

    private fun saveSlotsToFirebase() {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        val data = mapOf(dateKey to selectedSlots)

        db.collection("appointments")
            .document(doctorId)
            .set(data, SetOptions.merge()) // merge to avoid overwriting other days
            .addOnSuccessListener {
                Toast.makeText(this, "Slots saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
    }

    private var bookedSlots: List<String> = listOf()

    private fun loadSlotsFromFirebase() {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        db.collection("appointments").document(doctorId).get().addOnSuccessListener { doc ->
            val allSlots = (doc[dateKey] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            selectedSlots = allSlots

            // Change button text based on whether slots exist
            if (selectedSlots.isNotEmpty()) {
                btnAddSlots.text = "Edit Time Slot(s)"
            } else {
                btnAddSlots.text = "Add Time Slot(s)"
            }

            // Also get booked slots
            db.collection("confirmedAppointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", dateKey)
                .get()
                .addOnSuccessListener { confirmedDocs ->
                    bookedSlots = confirmedDocs.mapNotNull { it.getString("hour") }
                    updateTimeSlots()
                }
        }
    }

    private fun changeDay(offset: Int) {
        selectedDate.add(Calendar.DAY_OF_MONTH, offset)
        updateDateTitle()
        loadSlotsFromFirebase()
    }

    private fun showBookedSlotInfo(hour: String) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        db.collection("confirmedAppointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", dateKey)
            .whereEqualTo("hour", hour)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    Toast.makeText(this, "No booking info found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val booking = docs.first()
                val patientId = booking.getString("id") ?: "Unknown"
                val visitType = booking.getString("typeOfTheVisit") ?: "Unknown"

                // Now get patient info
                db.collection("users").document(patientId).get()
                    .addOnSuccessListener { patientDoc ->
                        val firstName = patientDoc.getString("firstName") ?: ""
                        val lastName = patientDoc.getString("lastName") ?: ""
                        val patientName = "$firstName $lastName".trim()

                        AlertDialog.Builder(this)
                            .setTitle("Booked Slot Info")
                            .setMessage("Patient: $patientName\nVisit Type: $visitType\nTime: $hour")
                            .setPositiveButton("Cancel Appointment") { _, _ ->
                                cancelAppointment(booking.id)
                            }
                            .setNegativeButton("Close", null)
                            .show()
                    }
            }
    }

    private fun cancelAppointment(appointmentDocId: String) {
        db.collection("confirmedAppointments").document(appointmentDocId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment canceled", Toast.LENGTH_SHORT).show()
                loadSlotsFromFirebase() // Refresh slots
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show()
            }
    }




}