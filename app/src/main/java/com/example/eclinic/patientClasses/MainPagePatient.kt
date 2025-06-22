package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
// import com.example.eclinic.chat.ChatPatientActivity // Usunięte
import com.example.eclinic.logRegClasses.LogRegActivity
import com.example.eclinic.logRegClasses.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

// Importuj nową aktywność z listą lekarzy
import com.example.eclinic.patientClasses.DoctorsListActivity // <-- Nowa nazwa aktywności
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


/**
 * [MainPagePatient] is the main dashboard activity for patients in the eClinic application.
 * It displays a welcome message, upcoming appointments, and provides navigation
 * to other patient-related functionalities like scheduling appointments, chat with doctors,
 * prescriptions, full appointments list, and profile settings via a bottom navigation bar.
 */
class MainPagePatient : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var scheduleAnApointment: ImageButton
    private lateinit var visitsRecyclerView: RecyclerView
    private lateinit var noVisitsTextView: TextView


    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the RecyclerView for upcoming visits,
     * fetches user data and upcoming visits from Firestore, and sets up navigation listeners.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page_patient)

        welcomeText = findViewById(R.id.petNameTextView)

        visitsRecyclerView = findViewById(R.id.upcomingVisitsRecyclerView)

        // Zamiana 8dp na px:
        val spacingInPixels = (8 * resources.displayMetrics.density).toInt()

        noVisitsTextView = findViewById(R.id.noVisitsTextView)

        visitsRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: android.view.View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.bottom = spacingInPixels
            }
        })
        visitsRecyclerView.layoutManager = LinearLayoutManager(this)


        bottomNav = findViewById(R.id.bottom_navigation)
        scheduleAnApointment = findViewById(R.id.ScheduleAnApointmentBtn)

        scheduleAnApointment.setOnClickListener {
            val intent = Intent(this, RegisterForAppointmentPatient::class.java)
            startActivity(intent)
        }

        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        welcomeText.text = "Welcome, $firstName"
                    }
                }

            fetchUpcomingVisit(uid)


        } else {
            welcomeText.text = "Welcome, Guest"
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, DoctorsListActivity::class.java))
                    true
                }

                R.id.nav_prescriptions -> {
                    startActivity(Intent(this, PrescriptionsActivity::class.java))
                    true
                }

                R.id.nav_appointments -> {
                    startActivity(Intent(this, AppointmentsActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     * This method is overridden to refresh the upcoming visits list when the activity resumes,
     * ensuring the displayed data is up-to-date.
     */
    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            fetchUpcomingVisit(uid)
        }
    }


    /**
     * Fetches the upcoming confirmed visits for a specific patient from Firebase Firestore.
     * It retrieves visits scheduled between today (inclusive) and 7 days from now (inclusive).
     * Doctor details are fetched for each visit, and the list is then sorted by date and time
     * before being displayed in the RecyclerView. Visibility of `noVisitsTextView` is adjusted.
     * @param patientId The UID of the patient whose upcoming visits are to be fetched.
     */
    private fun fetchUpcomingVisit(patientId: String) {
        val db = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Midnight today
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time

        // 7 days later
        val oneWeekLaterCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val oneWeekLater = oneWeekLaterCal.time

        db.collection("confirmedAppointments")
            .whereEqualTo("id", patientId)
            .get()
            .addOnSuccessListener { result ->

                val visits = mutableListOf<VisitItem>()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (doc in result.documents) {
                    val dateStr = doc.getString("date")
                    val hour = doc.getString("hour") ?: continue
                    val type = doc.getString("typeOfTheVisit") ?: "Visit"
                    val doctorId = doc.getString("doctorId") ?: continue
                    val price = doc.getString("price") ?: "Price"

                    val parsedDate = try { dateFormat.parse(dateStr!!) } catch (e: Exception) { null }

                    if (parsedDate != null && !parsedDate.before(today) && !parsedDate.after(oneWeekLater)) {                        val task = db.collection("users").document(doctorId).get()
                        .addOnSuccessListener { doctorDoc ->
                            val firstName = doctorDoc.getString("firstName") ?: ""
                            val lastName = doctorDoc.getString("lastName") ?: ""
                            val doctorName = "Dr. $firstName $lastName".trim()

                            visits.add(VisitItem(parsedDate, hour, type, doctorName, doc.id, price))
                        }
                        tasks.add(task)
                    }
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        visits.sortWith(compareBy<VisitItem> { it.date }.thenBy { visit ->
                            timeFormat.parse(visit.hour)?.time ?: 0
                        })

                        visitsRecyclerView.adapter = UpcomingVisitAdapter(visits)

                        if (visits.isEmpty()) {
                            noVisitsTextView.visibility = android.view.View.VISIBLE
                            visitsRecyclerView.visibility = android.view.View.GONE
                        } else {
                            noVisitsTextView.visibility = android.view.View.GONE
                            visitsRecyclerView.visibility = android.view.View.VISIBLE
                        }
                    }

            }
    }


}