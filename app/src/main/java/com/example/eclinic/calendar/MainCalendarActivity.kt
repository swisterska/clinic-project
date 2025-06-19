

package com.example.eclinic.calendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.patientClasses.MainPagePatient
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.SimpleDateFormat
import java.util.*

class MainCalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnConfirmVisit: MaterialButton

    private var doctorId: String? = null
    private var visitName: String? = null
    private var visitPrice: String? = null
    private var userRole: String? = null
    private var selectedDateKey: String? = null
    private var selectedSlot: String? = null
    private var availableSlots: List<String> = emptyList()
    private lateinit var textAvailableHours: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_calendar)

        firestore = FirebaseFirestore.getInstance()


        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.timeSlotsRecyclerView)
        btnConfirmVisit = findViewById(R.id.btnConfirmVisit)
        firestore = FirebaseFirestore.getInstance()

        textAvailableHours = findViewById(R.id.textAvailableHours)
        textAvailableHours.visibility = View.GONE

        doctorId = intent.getStringExtra("id")
        visitName = intent.getStringExtra("visitName")
        visitPrice = intent.getStringExtra("visitPrice")

        recyclerView.layoutManager = LinearLayoutManager(this)
        btnConfirmVisit.visibility = View.GONE

        btnConfirmVisit.setOnClickListener {
            selectedSlot?.let { bookAppointment(it) }
        }

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.today())
            .commit()

        fetchAvailableDatesAndHighlight()

        calendarView.setOnDateChangedListener { _, selectedDate, _ ->
            val selectedMillis = Calendar.getInstance().apply {
                set(selectedDate.year, selectedDate.month, selectedDate.day)
            }.timeInMillis

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDateKey = formatter.format(Date(selectedMillis))

            checkRoleAndPerformAction(selectedMillis) // Role check happens here ONLY after clicking
        }

    }

    private fun checkRoleAndPerformAction(selectedMillis: Long) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "PATIENT"
                        if (role == "DOCTOR") {
                            val intent = Intent(this, WeeklyScheduleActivityDoctor::class.java).apply {
                                putExtra("selectedDate", selectedMillis)
                                putExtra("id", doctorId)
                            }
                            startActivity(intent)
                        } else {
                            // PATIENT stays and loads slots
                            loadAvailableSlotsForPatient()
                        }
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
                }
        }
    }




    private fun bookAppointment(slot: String) {
            val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val visit = visitName ?: "Unknown"
            val price = visitPrice ?: "Unknown"

            val appointment = mapOf(
                "id" to patientId,
                "doctorId" to doctorId,
                "date" to selectedDateKey,
                "hour" to slot,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "typeOfTheVisit" to visit,
                "price" to price
            )

            firestore.collection("confirmedAppointments")
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

        private fun loadAvailableSlotsForPatient() {
            val dateKey = selectedDateKey ?: return
            val docId = doctorId ?: return

            firestore.collection("appointments").document(docId).get().addOnSuccessListener { doc ->
                val allSlots = (doc[dateKey] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                firestore.collection("confirmedAppointments")
                    .whereEqualTo("doctorId", docId)
                    .whereEqualTo("date", dateKey)
                    .get()
                    .addOnSuccessListener { confirmedDocs ->
                        val bookedSlots = confirmedDocs.mapNotNull { it.getString("hour") }
                        availableSlots = allSlots.filter { it !in bookedSlots }
                            .sortedBy { it }

                        if (availableSlots.isEmpty()) {
                            textAvailableHours.text = "No available hours."
                            textAvailableHours.visibility = View.VISIBLE
                            recyclerView.adapter = null
                        } else {
                            textAvailableHours.text = "Available hours:"
                            textAvailableHours.visibility = View.VISIBLE
                            recyclerView.adapter = TimeSlotDisplayAdapter(
                                availableSlots,
                                selectedSlot,
                                onSlotSelected = { slot ->
                                    selectedSlot = slot
                                    btnConfirmVisit.visibility = View.VISIBLE
                                    loadAvailableSlotsForPatient() // Refresh to update selection
                                }

                            )
                        }

                        recyclerView.adapter = TimeSlotDisplayAdapter(
                            availableSlots,
                            selectedSlot,
                            onSlotSelected = { slot ->
                                selectedSlot = slot
                                btnConfirmVisit.visibility = View.VISIBLE
                                loadAvailableSlotsForPatient()
                            }
                        )


                    }
            }

        }


        private fun fetchAvailableDatesAndHighlight() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())

        val appointmentsRef = firestore.collection("appointments").document(doctorId!!)
        val confirmedRef = firestore.collection("confirmedAppointments")

        appointmentsRef.get().addOnSuccessListener { doc ->
            val appointmentsData = doc.data.orEmpty()

            // Load all confirmed appointments for this doctor
            confirmedRef.whereEqualTo("doctorId", doctorId).get()
                .addOnSuccessListener { confirmedDocs ->

                    val confirmedMap = mutableMapOf<String, MutableList<String>>()

                    for (booking in confirmedDocs) {
                        val date = booking.getString("date") ?: continue
                        val hour = booking.getString("hour") ?: continue
                        confirmedMap.getOrPut(date) { mutableListOf() }.add(hour)
                    }

                    val greenDates = mutableListOf<CalendarDay>()
                    val redDates = mutableListOf<CalendarDay>()

                    for ((key, value) in appointmentsData) {
                        val slots = (value as? List<*>)?.filterIsInstance<String>() ?: continue
                        val date = formatter.parse(key) ?: continue

                        if (key >= today) {
                            val bookedSlots = confirmedMap[key] ?: emptyList()
                            val availableSlots = slots - bookedSlots

                            if (availableSlots.isEmpty()) {
                                redDates.add(CalendarDay.from(date))
                            } else {
                                greenDates.add(CalendarDay.from(date))
                            }
                        }
                    }

                    // Apply green dot decorator
                    calendarView.addDecorator(object : DayViewDecorator {
                        override fun shouldDecorate(day: CalendarDay): Boolean {
                            return greenDates.contains(day)
                        }

                        override fun decorate(view: DayViewFacade) {
                            view.addSpan(DotSpan(10f, ContextCompat.getColor(this@MainCalendarActivity, R.color.teal_200)))
                        }
                    })

                    // Apply red dot decorator
                    calendarView.addDecorator(object : DayViewDecorator {
                        override fun shouldDecorate(day: CalendarDay): Boolean {
                            return redDates.contains(day)
                        }

                        override fun decorate(view: DayViewFacade) {
                            view.addSpan(DotSpan(10f, ContextCompat.getColor(this@MainCalendarActivity, R.color.red)))
                        }
                    })
                }
        }
    }

}
