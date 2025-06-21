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
import com.example.eclinic.chat.ChatUtils
import com.example.eclinic.patientClasses.MainPagePatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp
import com.example.eclinic.doctorClasses.PrescriptionsDocActivity

/**
 * Activity for patients to view a doctor's weekly schedule and book appointments.
 * This activity displays the available time slots for a selected doctor and date,
 * allows the patient to select a slot, and confirms the booking.
 *
 * It handles:
 * - Displaying the selected date and navigating between days.
 * - Loading available and booked slots from Firebase Firestore.
 * - Filtering out past slots for the current day.
 * - Allowing patients to select a time slot and confirm an appointment.
 * - Sending a confirmation message to the patient after booking.
 */
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

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves doctor ID and selected date from the intent,
     * sets up click listeners for navigation and appointment confirmation, and loads initial data.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_schedule)

        doctorId = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Hide the "Add Slots" button as it's not relevant for patients
        findViewById<Button>(R.id.btnAddSlots)?.visibility = View.GONE


        val selectedDateMillis = intent.getLongExtra("selectedDate", 0L)
        selectedDate = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

        dateTitle = findViewById(R.id.dateTitle)
        btnConfirmVisit = findViewById(R.id.btnConfirmVisit)
        recyclerView = findViewById(R.id.recyclerView)

        // Initially hide the confirm button until a slot is selected
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

    /**
     * Updates the text of the [dateTitle] TextView to display the currently selected date.
     */
    private fun updateDateTitle() {
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        dateTitle.text = formatter.format(selectedDate.time)
    }

    /**
     * Updates the [RecyclerView] displaying time slots with the current available slots.
     * Sets up the [TimeSlotDisplayAdapter] and its click listener for patient slot selection.
     */
    private fun updateTimeSlots() {
        val adapter = TimeSlotDisplayAdapter(
            selectedSlots,
            selectedSlot = selectedPatientSlot,
            onSlotSelected = { selectedTime ->
                selectedPatientSlot = selectedTime
                btnConfirmVisit.visibility = Button.VISIBLE // Show confirm button when a slot is selected
                updateTimeSlots() // Re-render to highlight the selected slot
            }
        )
        recyclerView.adapter = adapter
    }


    /**
     * Loads available and booked time slots for the [selectedDate] from Firebase Firestore.
     * Filters out already booked slots and, for the current day, filters out past time slots.
     * Updates the [selectedSlots] list and then refreshes the UI.
     */
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
                                slotCalendar.after(now) // Only show slots that are in the future
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


    /**
     * Books an appointment for the currently authenticated patient with the selected doctor
     * at the chosen time slot. Saves the appointment details to Firebase Firestore
     * and sends a confirmation chat message to the patient.
     * Navigates the patient back to the [MainPagePatient] after successful booking.
     * @param slot The selected time slot for the appointment (e.g., "10:30").
     */
    private fun bookAppointment(slot: String) {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val visitName = intent.getStringExtra("visitName") ?: "Unknown"
        val visitPrice = intent.getStringExtra("visitPrice") ?: "Unknown"

        val appointment = mapOf(
            "id" to patientId,
            "doctorId" to doctorId,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time),
            "hour" to slot,
            "timestamp" to Timestamp.now(),
            "typeOfTheVisit" to visitName,
            "price" to visitPrice
        )

        db.collection("confirmedAppointments")
            .add(appointment)
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show()

                // send a message to the patient
                ChatUtils.sendMessage(
                    fromId = doctorId, // Assuming the doctor ID is available or can be set to a system ID
                    toId = patientId,
                    messageText = "Thank you for booking a visit with me. If you have any questions, feel free to reach out before the appointment!"
                )

                val intent = Intent(this, MainPagePatient::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Booking failed.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Changes the [selectedDate] by the given [offset] (in days).
     * After changing the date, it updates the date title and reloads slots from Firebase.
     * @param offset The number of days to change (e.g., -1 for previous day, 1 for next day).
     */
    private fun changeDay(offset: Int) {
        selectedDate.add(Calendar.DAY_OF_MONTH, offset)
        updateDateTitle()
        loadSlotsFromFirebase()
    }

}