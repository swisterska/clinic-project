package com.example.eclinic.calendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.patientClasses.MainPagePatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp

class WeeklyScheduleActivityPatient : AppCompatActivity() {

    private lateinit var dateTitle: TextView
    private lateinit var btnConfirmVisit: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var doctorId: String
    private lateinit var selectedDate: Calendar
    private var selectedSlots: List<String> = listOf()
    private var selectedPatientSlot: String? = null
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
                    val availableSlots = allSlots.filter { it !in bookedSlots }

                    // Check if selected date is today
                    val now = Calendar.getInstance()
                    val isToday = selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

                    selectedSlots = if (isToday) {
                        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

                        // Add 10-minute margin to current time
                        now.add(Calendar.MINUTE, 10)

                        availableSlots.filter { timeStr ->
                            try {
                                val slotTime = formatter.parse(timeStr) ?: return@filter false
                                val slotCalendar = Calendar.getInstance().apply {
                                    time = slotTime
                                    set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                                    set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                                }
                                slotCalendar.after(now)
                            } catch (e: Exception) {
                                false
                            }
                        }
                    } else {
                        availableSlots
                    }

                    updateTimeSlots()
                }
        }
    }



    private fun bookAppointment(slot: String) {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val visitName = intent.getStringExtra("visitName") ?: "Unknown"
        val visitPrice = intent.getStringExtra("visitPrice") ?: "Unknown"

        val appointment = mapOf(
            "id" to patientId,
            "doctorId" to doctorId,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time),
            "hour" to slot,
            "timestamp" to Timestamp.now(),  // ← Correct format
            "typeOfTheVisit" to visitName,
            "price" to visitPrice
        )

        db.collection("confirmedAppointments")
            .add(appointment)
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainPagePatient::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Booking failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun changeDay(offset: Int) {
        selectedDate.add(Calendar.DAY_OF_MONTH, offset)
        updateDateTitle()
        loadSlotsFromFirebase()
    }

}
