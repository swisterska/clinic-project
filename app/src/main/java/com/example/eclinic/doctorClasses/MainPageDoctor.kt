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
 * Main page activity for doctors.
 *
 * Displays a welcome message with the doctor's first name,
 * shows the list of today's confirmed visits,
 * and provides navigation to chat with patients, prescriptions, calendar, and profile.
 */
class MainPageDoctor : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var todayVisitsHeader: TextView
    private lateinit var rvTodayVisits: RecyclerView
    private lateinit var bottomNavDoc: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var userId: String? = null

    /**
     * Called when the activity is starting.
     *
     * Initializes views, fetches the current user's ID and loads their profile name,
     * loads today's visits for the doctor,
     * and sets up bottom navigation item listeners.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page_doctor)

        welcomeText = findViewById(R.id.tvWelcomeMessage)
        todayVisitsHeader = findViewById(R.id.tvTodayVisitsHeader)
        rvTodayVisits = findViewById(R.id.rvTodayVisits)
        bottomNavDoc = findViewById(R.id.bottom_navigation_doctor)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Fetch doctor's first name to display welcome message
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        welcomeText.text = "Welcome, dr. $firstName"
                    }
                }
                .addOnFailureListener {
                    welcomeText.text = "Welcome, Doctor"
                }

            fetchTodayVisitsForDoctor(userId)
        }

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
                    if (userId != null) {
                        val intent = Intent(this, MainCalendarActivity::class.java)
                        intent.putExtra("id", userId)
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
                else -> false
            }
        }

        rvTodayVisits.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Called after the activity has been resumed.
     *
     * Reloads today's visits to ensure the displayed list is up to date.
     */
    override fun onResume() {
        super.onResume()
        userId?.let { fetchTodayVisitsForDoctor(it) }  // Reload visits on each resume
    }

    /**
     * Fetches confirmed visits for today for the given doctor.
     *
     * Queries Firestore for confirmed appointments for the specified doctor on the current date,
     * retrieves patient names for each visit, and populates the RecyclerView adapter.
     *
     * @param doctorId The UID of the doctor for whom to fetch visits.
     */
    private fun fetchTodayVisitsForDoctor(doctorId: String) {
        val db = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val todayStr = dateFormat.format(today)

        db.collection("confirmedAppointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", todayStr)
            .get()
            .addOnSuccessListener { result ->

                val visits = mutableListOf<VisitItem>()
                val patientTasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (doc in result.documents) {
                    val dateStr = doc.getString("date") ?: continue
                    val hour = doc.getString("hour") ?: continue
                    val type = doc.getString("typeOfTheVisit") ?: "Visit"
                    val patientId = doc.getString("id") ?: continue
                    val price = doc.getString("price") ?: ""

                    val documentId = doc.id

                    val parsedDate = try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    } ?: continue

                    val patientTask = db.collection("users").document(patientId).get()
                        .addOnSuccessListener { patientDoc ->
                            val firstName = patientDoc.getString("firstName") ?: ""
                            val lastName = patientDoc.getString("lastName") ?: ""
                            val patientName = "$firstName $lastName".trim()

                            visits.add(VisitItem(parsedDate, hour, type, patientName, documentId, price))
                        }

                    patientTasks.add(patientTask)
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(patientTasks)
                    .addOnSuccessListener {
                        visits.sortWith(compareBy { it.hour })

                        runOnUiThread {
                            rvTodayVisits.adapter = TodayVisitsAdapter(visits)
                            todayVisitsHeader.text = "You have ${visits.size} visits for today"
                        }
                    }
            }
    }
}
