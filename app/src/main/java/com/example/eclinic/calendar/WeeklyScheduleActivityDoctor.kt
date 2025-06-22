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
 * Activity for doctors to manage their weekly schedule, including adding, editing,
 * and viewing available and booked time slots. Doctors can also cancel appointments.
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
     * Initializes UI components, retrieves doctor ID and selected date from intent,
     * sets up click listeners, and loads initial data.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].  Otherwise it is null.
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
     * Updates the text of the [dateTitle] TextView to display the currently selected date
     * in a formatted string (e.g., "Monday, Jan 01").
     */
    private fun updateDateTitle() {
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        dateTitle.text = formatter.format(selectedDate.time)
    }

    /**
     * Updates the RecyclerView with the current list of available and booked time slots.
     * Creates and sets a new [TimeSlotDisplayAdapter] for the RecyclerView.
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
     * Callback method from [TimeSlotDialog] when new time slots are selected by the doctor.
     * Updates the [selectedSlots] list, refreshes the UI, and saves the new slots to Firebase.
     * @param slots The list of selected time slot strings (e.g., "09:00 - 09:30").
     */
    override fun onSlotsSelected(slots: List<String>) {
        selectedSlots = slots
        updateTimeSlots()
        saveSlotsToFirebase()
    }

    /**
     * Saves the currently selected time slots for the [selectedDate] to the Firebase Firestore database.
     * The slots are stored under the doctor's ID, merged with existing data to avoid overwriting
     * slots for other days.
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
     * Loads available and booked time slots for the [selectedDate] from Firebase Firestore.
     * Updates the [selectedSlots] and [bookedSlots] lists and refreshes the RecyclerView.
     * Also updates the text of the "Add Slots" button based on whether slots exist for the day.
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
     * Changes the [selectedDate] by the given offset (e.g., -1 for previous day, 1 for next day).
     * Updates the date title and reloads slots from Firebase for the new date.
     * @param offset The number of days to add to or subtract from the current date.
     */
    private fun changeDay(offset: Int) {
        selectedDate.add(Calendar.DAY_OF_MONTH, offset)
        updateDateTitle()
        loadSlotsFromFirebase()
    }

    /**
     * Displays an AlertDialog with information about a booked time slot.
     * Fetches details like patient name and visit type from Firebase and provides an option
     * to cancel the appointment.
     * @param hour The specific hour of the booked slot (e.g., "10:00 - 10:30").
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
     * Cancels a confirmed appointment by deleting its record from the Firebase Firestore database.
     * After cancellation, it refreshes the time slots to reflect the change.
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