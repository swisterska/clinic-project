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


class MainPagePatient : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var scheduleAnApointment: ImageButton
    private lateinit var visitsRecyclerView: RecyclerView
    private lateinit var noVisitsTextView: TextView


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

    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            fetchUpcomingVisit(uid)
        }
    }


    private fun fetchUpcomingVisit(patientId: String) {
        val db = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val today = Calendar.getInstance().time

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

                    val parsedDate = try { dateFormat.parse(dateStr!!) } catch (e: Exception) { null }

                    if (parsedDate != null && parsedDate.after(today)) {
                        val task = db.collection("users").document(doctorId).get()
                            .addOnSuccessListener { doctorDoc ->
                                val firstName = doctorDoc.getString("firstName") ?: ""
                                val lastName = doctorDoc.getString("lastName") ?: ""
                                val doctorName = "Dr. $firstName $lastName".trim()

                                visits.add(VisitItem(parsedDate, hour, type, doctorName, doc.id))
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
