package com.example.eclinic.patientClasses

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * [AppointmentsActivity] displays a list of a patient's upcoming confirmed appointments.
 * It fetches appointment data from Firebase Firestore, displays it in a RecyclerView,
 * and allows patients to cancel appointments.
 */
class AppointmentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private lateinit var loadingBar: ProgressBar
    private lateinit var emptyText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private lateinit var switchPastAppointments: com.google.android.material.switchmaterial.SwitchMaterial


    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the RecyclerView with its adapter and layout manager,
     * and initiates the loading of appointments.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appointments)

        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        loadingBar = findViewById(R.id.loadingBar)
        emptyText = findViewById(R.id.emptyText)

        switchPastAppointments = findViewById(R.id.switchPastAppointments)
        switchPastAppointments.setOnCheckedChangeListener { _, _ ->
            loadAppointments()
        }


        // Initialize the adapter with an empty list and a callback for item clicks (cancel action)
        adapter = AppointmentsAdapter(mutableListOf()) { visit ->
            confirmAndCancelAppointment(visit.documentId)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadAppointments()
    }

    /**
     * Called when the activity will start interacting with the user.
     * This is a good place to load appointments to ensure the list is up-to-date
     * when the user returns to this activity.
     */
    override fun onResume() {
        super.onResume()
        loadAppointments()
    }

    /**
     * Loads the current user's confirmed appointments from Firebase Firestore.
     * It filters appointments to show only those scheduled from today onwards
     * or views past appointments if the switch is on.
     * Appointment details, including doctor's name, are fetched asynchronously.
     * The list is then sorted by date and time and displayed in the RecyclerView.
     * Progress bar and empty text visibility are managed during the loading process.
     */
    private fun loadAppointments() {
        loadingBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        // Get the current user's UID; return if null (not logged in)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Calculate midnight today for filtering future appointments
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time


        // Query "confirmedAppointments" collection where 'id' (patientId) matches current user's UID
        db.collection("confirmedAppointments")
            .whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { result ->
                val visits = mutableListOf<VisitItem>()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()
                val now = Calendar.getInstance().time // Current time for filtering out past appointments

                // Iterate through each appointment document
                for (doc in result.documents) {
                    val dateStr = doc.getString("date") ?: continue
                    val hour = doc.getString("hour") ?: continue
                    val type = doc.getString("typeOfTheVisit") ?: "Visit"
                    val price = doc.getString("price") ?: "Price"
                    val doctorId = doc.getString("doctorId") ?: continue

                    val parsedDate = try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }

                    if (parsedDate != null) {
                        val showPast = switchPastAppointments.isChecked
                        val isPastAppointment = parsedDate.before(today) ||
                                (parsedDate == today && SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .parse(hour)?.before(Calendar.getInstance().time) == true)

                        if ((showPast && isPastAppointment) || (!showPast && !isPastAppointment)) {
                            val docId = doc.id
                            val task = db.collection("users").document(doctorId).get()
                                .addOnSuccessListener { doctorDoc ->
                                    val firstName = doctorDoc.getString("firstName") ?: ""
                                    val lastName = doctorDoc.getString("lastName") ?: ""
                                    val doctorName = "Dr. $firstName $lastName".trim()
                                    visits.add(
                                        VisitItem(
                                            parsedDate,
                                            hour,
                                            type,
                                            doctorName,
                                            docId,
                                            price,
                                            isPastAppointment
                                        )
                                    )
                                }
                            tasks.add(task)
                        }
                    }
                }


                // Execute all doctor fetching tasks concurrently and then update UI
                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        // Sort the visits by date, then by hour
                        val showPast = switchPastAppointments.isChecked
                        if (showPast) {
                            // Sort descending: newest past appointment first
                            visits.sortWith(
                                compareByDescending<VisitItem> { it.date }.thenByDescending {
                                    hourFormat.parse(it.hour)?.time ?: 0
                                }
                            )
                        } else {
                            // Sort ascending: soonest upcoming appointment first
                            visits.sortWith(
                                compareBy<VisitItem> { it.date }.thenBy {
                                    hourFormat.parse(it.hour)?.time ?: 0
                                }
                            )
                        }

                        loadingBar.visibility = View.GONE // Hide loading bar
                        if (visits.isEmpty()) {
                            emptyText.visibility = View.VISIBLE // Show empty message if no appointments
                        } else {
                            emptyText.visibility = View.GONE // Hide empty message
                        }
                        adapter.updateAppointments(visits) // Update RecyclerView adapter
                    }
            }
            .addOnFailureListener {
                // Handle errors during data loading
                loadingBar.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
                Toast.makeText(this, "Failed to load appointments.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Displays an [AlertDialog] to confirm with the user if they want to cancel an appointment.
     * If confirmed, it calls [cancelAppointment].
     * @param docId The document ID of the appointment to be cancelled.
     */
    private fun confirmAndCancelAppointment(docId: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes") { _, _ -> cancelAppointment(docId) } // If "Yes", call cancelAppointment
            .setNegativeButton("No", null) // If "No", do nothing
            .show()
    }

    /**
     * Deletes the specified appointment from the "confirmedAppointments" collection in Firestore.
     * On success, it shows a toast message and reloads the appointments list.
     * On failure, it shows a failure toast message.
     * @param docId The document ID of the appointment to be deleted.
     */
    private fun cancelAppointment(docId: String) {
        db.collection("confirmedAppointments").document(docId)
            .delete() // Delete the document
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                loadAppointments() // Reload appointments to reflect the change
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
            }
    }
}