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


/**
 * Activity for doctors to manage their weekly schedule, including adding/editing available time slots
 * and viewing/canceling booked appointments.
 *
 * This activity allows doctors to:
 * - Display the selected date's available and booked time slots.
 * - Add or edit available time slots for the selected date using a [TimeSlotDialog].
 * - Navigate between days to view and manage schedules for different dates.
 * - View details of booked appointments and cancel them.
 *
 * It communicates with Firebase Firestore to store and retrieve time slot and appointment data.
 */
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

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves the doctor ID and selected date from the intent,
     * sets up click listeners for navigation and slot management, and loads initial data.
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


    /**
     * Updates the text of the [dateTitle] TextView to display the currently selected date.
     */
    private fun updateDateTitle() {
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        dateTitle.text = formatter.format(selectedDate.time)
    }

    /**
     * Updates the [RecyclerView] displaying time slots with the current available and booked slots.
     * Sets up the [TimeSlotDisplayAdapter] and its click listener for booked slots.
     */
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

    /**
     * Callback method from [TimeSlotDialog.TimeSlotListener] when the user confirms
     * their selection of time slots.
     * Updates the [selectedSlots] and triggers an update of the UI and saving to Firebase.
     * @param slots A [List] of selected time slot strings.
     */
    override fun onSlotsSelected(slots: List<String>) {
        selectedSlots = slots
        updateTimeSlots()
        saveSlotsToFirebase()
    }

    /**
     * Saves the currently selected time slots for the [selectedDate] to Firebase Firestore.
     * It uses [SetOptions.merge] to avoid overwriting existing data for other dates.
     */
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

    /**
     * Loads the available and booked time slots for the [selectedDate] from Firebase Firestore.
     * Updates the [selectedSlots] and [bookedSlots] lists and then refreshes the UI.
     * Also updates the text of [btnAddSlots] based on whether slots exist for the current day.
     */
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

    /**
     * Displays an [AlertDialog] with information about a booked time slot.
     * Fetches patient details and visit type from Firebase Firestore for the specified hour.
     * Provides an option to cancel the appointment.
     * @param hour The time string of the booked slot (e.g., "09:30").
     */
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

    /**
     * Cancels a confirmed appointment by deleting its document from the "confirmedAppointments" collection in Firestore.
     * After successful cancellation, it refreshes the time slots displayed.
     * @param appointmentDocId The document ID of the appointment to be canceled.
     */
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