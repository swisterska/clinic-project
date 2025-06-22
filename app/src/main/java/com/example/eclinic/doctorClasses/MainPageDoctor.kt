package com.example.eclinic.doctorClasses

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.calendar.MainCalendarActivity
import com.example.eclinic.patientClasses.PatientsListActivity
import com.example.eclinic.patientClasses.VisitItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main activity for doctors in the eClinic application.
 * This activity displays a welcome message, a list of today's scheduled visits for the doctor,
 * and provides navigation to other doctor-specific features like chat, prescriptions, calendar, and profile.
 */
class MainPageDoctor : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var todayVisitsHeader: TextView
    private lateinit var rvTodayVisits: RecyclerView
    private lateinit var bottomNavDoc: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var userId: String? = null // This field is declared but not initialized in onCreate, though a local 'userId' is used.

    /**
     * Called when the activity is first created.
     * Initializes UI components, Firebase instances, fetches and displays doctor's welcome message
     * and today's visits, and sets up bottom navigation listeners.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    @SuppressLint("MissingInflatedId") // Suppresses a lint warning, ensure all IDs are correctly linked in XML
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page_doctor)

        // Initialize UI components
        welcomeText = findViewById(R.id.tvWelcomeMessage)
        todayVisitsHeader = findViewById(R.id.tvTodayVisitsHeader)
        rvTodayVisits = findViewById(R.id.rvTodayVisits)
        bottomNavDoc = findViewById(R.id.bottom_navigation_doctor)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get the current authenticated user's ID
        val currentUserId = auth.currentUser?.uid
        this.userId = currentUserId // Assign to the class-level property for use in onResume

        if (currentUserId != null) {
            // Fetch doctor's first name to personalize the welcome message
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        welcomeText.text = "Welcome, dr. $firstName"
                    }
                }
                .addOnFailureListener {
                    // Fallback welcome message if fetching name fails
                    welcomeText.text = "Welcome, Doctor"
                }

            // Fetch and display today's visits for the doctor
            fetchTodayVisitsForDoctor(currentUserId)
        }

        // Set up bottom navigation item selection listener
        bottomNavDoc.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chat -> {
                    startActivity(Intent(this, PatientsListActivity::class.java))
                    true
                }
                R.id.navigation_prescriptions_doc -> {
                    startActivity(Intent(this, PrescriptionsDocActivity::class.java))
                    true
                }
                R.id.navigation_calendar -> {
                    if (currentUserId != null) {
                        val intent = Intent(this, MainCalendarActivity::class.java)
                        intent.putExtra("id", currentUserId) // Pass doctor ID to calendar activity
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, DoctorProfileActivity::class.java))
                    true
                }
                else -> false // Should not happen with defined menu items
            }
        }

        // Set up LinearLayoutManager for the RecyclerView to display visits
        rvTodayVisits.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Called when the activity will start interacting with the user.
     * This is a good place to restart any processes that need to be active while the activity is resumed.
     * In this case, it reloads today's visits to ensure the list is up-to-date.
     */
    override fun onResume() {
        super.onResume()
        // Reload visits whenever the activity comes to the foreground
        userId?.let { fetchTodayVisitsForDoctor(it) } // <-- reloads visits on each entry
    }

    /**
     * Fetches today's confirmed appointments for the given doctor from Firebase Firestore.
     * It retrieves patient details for each appointment and updates the RecyclerView.
     * @param doctorId The Firebase User ID (UID) of the doctor.
     */
    private fun fetchTodayVisitsForDoctor(doctorId: String) {
        val db = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val todayStr = dateFormat.format(today) // Get today's date in "YYYY-MM-DD" format

        // Query confirmed appointments for the current doctor and today's date
        db.collection("confirmedAppointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", todayStr)
            .get()
            .addOnSuccessListener { result ->
                val visits = mutableListOf<VisitItem>()
                // List to hold tasks for fetching patient details, to ensure all are complete before updating UI
                val patientTasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (doc in result.documents) {
                    // Extract appointment details
                    val dateStr = doc.getString("date") ?: continue
                    val hour = doc.getString("hour") ?: continue
                    val type = doc.getString("typeOfTheVisit") ?: "Visit"
                    val patientId = doc.getString("id") ?: continue // 'id' field stores patientId
                    val price = doc.getString("price") ?: ""

                    val documentId = doc.id // Document ID of the confirmed appointment

                    val parsedDate = try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    } ?: continue

                    // Fetch patient details (firstName, lastName) for each visit
                    val patientTask = db.collection("users").document(patientId).get()
                        .addOnSuccessListener { patientDoc ->
                            val firstName = patientDoc.getString("firstName") ?: ""
                            val lastName = patientDoc.getString("lastName") ?: ""
                            val patientName = "$firstName $lastName".trim() // Combine first and last name

                            // Add the VisitItem to the list
                            visits.add(VisitItem(parsedDate, hour, type, patientName, documentId, price))
                        }
                    patientTasks.add(patientTask)
                }

                // Wait for all patient detail fetching tasks to complete
                com.google.android.gms.tasks.Tasks.whenAllComplete(patientTasks)
                    .addOnSuccessListener {
                        // Sort visits by hour after all patient details are loaded
                        visits.sortWith(compareBy { it.hour })

                        // Update UI on the main thread
                        runOnUiThread {
                            rvTodayVisits.adapter = TodayVisitsAdapter(visits)
                            todayVisitsHeader.text = "You have ${visits.size} visits for today"
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle errors if any patient task fails
                        Toast.makeText(this, "Failed to load patient details for visits: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load today's visits: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}