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
import com.example.eclinic.chat.ChatUtils
import com.example.eclinic.patientClasses.MainPagePatient
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp
import com.example.eclinic.doctorClasses.PrescriptionsDocActivity

/**
 * Main calendar activity that serves as an entry point for both doctors and patients
 * to view and manage schedules.
 *
 * This activity handles:
 * - Displaying a calendar with available and fully booked dates highlighted.
 * - Redirecting doctors to [WeeklyScheduleActivityDoctor] when they select a date.
 * - Allowing patients to view available time slots for a selected date and book appointments.
 * - Interacting with Firebase Firestore to fetch and update appointment data.
 */
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


    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up Firebase instances, retrieves intent data,
     * configures the calendar view, and sets up click listeners.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
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

        doctorId = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        visitName = intent.getStringExtra("visitName")
        visitPrice = intent.getStringExtra("visitPrice")

        recyclerView.layoutManager = LinearLayoutManager(this)
        btnConfirmVisit.visibility = View.GONE

        btnConfirmVisit.setOnClickListener {
            selectedSlot?.let { bookAppointment(it) }
        }

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.today()) // Prevent selecting past dates
            .commit()

        fetchAvailableDatesAndHighlight()

        calendarView.setOnDateChangedListener { _, selectedDate, _ ->
            val selectedMillis = Calendar.getInstance().apply {
                set(selectedDate.year, selectedDate.month, selectedDate.day)
            }.timeInMillis

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDateKey = formatter.format(Date(selectedMillis))

            // Check user role and perform action (doctor vs. patient flow)
            checkRoleAndPerformAction(selectedMillis)
        }

    }

    /**
     * Checks the role of the current authenticated user (Doctor or Patient) and
     * performs an action based on the role after a date is selected on the calendar.
     * - If the user is a "DOCTOR", it launches [WeeklyScheduleActivityDoctor].
     * - If the user is a "PATIENT", it loads available time slots for the selected date.
     * @param selectedMillis The selected date in milliseconds.
     */
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
                            // PATIENT stays and loads slots for booking
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


    /**
     * Books an appointment for the currently authenticated patient with the selected doctor
     * at the chosen time slot. Saves the appointment details to Firebase Firestore
     * and sends a confirmation chat message to the patient.
     * Navigates the patient back to the [MainPagePatient] after successful booking.
     * @param slot The selected time slot for the appointment (e.g., "10:30").
     */
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

                // Send a message to the patient confirming the booking
                ChatUtils.sendMessage(
                    fromId = doctorId.toString(), // Assuming doctorId is the sender
                    toId = patientId,
                    messageText = "Thank you for booking a visit with me. If you have any questions, feel free to reach out before the appointment!"
                )

                val intent = Intent(this, MainPagePatient::class.java)
                // Clear the activity stack to prevent going back to calendar after booking
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Booking failed.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Loads the available time slots for the selected date and doctor from Firebase Firestore
     * for patient view. It filters out already booked slots and updates the [RecyclerView].
     * If no slots are available, a corresponding message is displayed.
     */
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
                        .sortedBy { it } // Sort slots for consistent display

                    if (availableSlots.isEmpty()) {
                        textAvailableHours.text = "No available hours."
                        textAvailableHours.visibility = View.VISIBLE
                        recyclerView.adapter = null // Clear previous adapter
                        btnConfirmVisit.visibility = View.GONE // Hide confirm button
                    } else {
                        textAvailableHours.text = "Available hours:"
                        textAvailableHours.visibility = View.VISIBLE
                        recyclerView.adapter = TimeSlotDisplayAdapter(
                            availableSlots,
                            selectedSlot,
                            onSlotSelected = { slot ->
                                selectedSlot = slot
                                btnConfirmVisit.visibility = View.VISIBLE
                                loadAvailableSlotsForPatient() // Refresh to update selection highlight
                            }

                        )
                    }

                    // This block seems redundant as it's duplicating the adapter assignment.
                    // The first assignment within the if/else block should suffice.
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


    /**
     * Fetches all available and booked dates for the current doctor from Firestore
     * and applies decorators to the [MaterialCalendarView] to highlight them.
     * - Dates with at least one available slot are marked with a green dot.
     * - Dates with no available slots (but previously had some) are marked with a red dot.
     * Only highlights dates from today onwards.
     */
    private fun fetchAvailableDatesAndHighlight() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())

        val appointmentsRef = firestore.collection("appointments").document(doctorId!!)
        val confirmedRef = firestore.collection("confirmedAppointments")

        appointmentsRef.get().addOnSuccessListener { doc ->
            val appointmentsData = doc.data.orEmpty()

            // Load all confirmed appointments for this doctor to determine booked slots
            confirmedRef.whereEqualTo("doctorId", doctorId).get()
                .addOnSuccessListener { confirmedDocs ->

                    val confirmedMap = mutableMapOf<String, MutableList<String>>()

                    // Populate confirmedMap: date -> list of booked hours
                    for (booking in confirmedDocs) {
                        val date = booking.getString("date") ?: continue
                        val hour = booking.getString("hour") ?: continue
                        confirmedMap.getOrPut(date) { mutableListOf() }.add(hour)
                    }

                    val greenDates = mutableListOf<CalendarDay>() // Dates with available slots
                    val redDates = mutableListOf<CalendarDay>()    // Dates with no available slots (fully booked/empty)

                    for ((key, value) in appointmentsData) {
                        val slots = (value as? List<*>)?.filterIsInstance<String>() ?: continue
                        val date = formatter.parse(key) ?: continue

                        // Only consider dates from today onwards for highlighting
                        if (key >= today) {
                            val bookedSlots = confirmedMap[key] ?: emptyList()
                            val availableSlots = slots - bookedSlots // Calculate truly available slots

                            if (availableSlots.isEmpty()) {
                                redDates.add(CalendarDay.from(date))
                            } else {
                                greenDates.add(CalendarDay.from(date))
                            }
                        }
                    }

                    // Apply green dot decorator for dates with available slots
                    calendarView.addDecorator(object : DayViewDecorator {
                        override fun shouldDecorate(day: CalendarDay): Boolean {
                            return greenDates.contains(day)
                        }

                        override fun decorate(view: DayViewFacade) {
                            view.addSpan(DotSpan(10f, ContextCompat.getColor(this@MainCalendarActivity, R.color.teal_200)))
                        }
                    })

                    // Apply red dot decorator for dates with no available slots
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