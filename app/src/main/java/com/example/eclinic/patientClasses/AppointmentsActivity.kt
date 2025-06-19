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

class AppointmentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private lateinit var loadingBar: ProgressBar
    private lateinit var emptyText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appointments)

        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        loadingBar = findViewById(R.id.loadingBar)
        emptyText = findViewById(R.id.emptyText)

        adapter = AppointmentsAdapter(mutableListOf()) { visit ->
            confirmAndCancelAppointment(visit.documentId)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadAppointments()
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }


    private fun loadAppointments() {
        loadingBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Midnight today
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time


        db.collection("confirmedAppointments")
            .whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { result ->
                val visits = mutableListOf<VisitItem>()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()
                val now = Calendar.getInstance().time

                for (doc in result.documents) {
                    val dateStr = doc.getString("date")
                    val hour = doc.getString("hour") ?: continue
                    val type = doc.getString("typeOfTheVisit") ?: "Visit"
                    val price = doc.getString("price") ?: "Price"
                    val doctorId = doc.getString("doctorId") ?: continue

                    val parsedDate = try { dateFormat.parse(dateStr!!) } catch (e: Exception) { null }
                    if (parsedDate != null && !parsedDate.before(today)) {                        val docId = doc.id
                        val task = db.collection("users").document(doctorId).get()
                            .addOnSuccessListener { doctorDoc ->
                                val firstName = doctorDoc.getString("firstName") ?: ""
                                val lastName = doctorDoc.getString("lastName") ?: ""
                                val doctorName = "Dr. $firstName $lastName".trim()
                                visits.add(VisitItem(parsedDate, hour, type, doctorName, docId, price))
                            }
                        tasks.add(task)
                    }
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        visits.sortWith(
                            compareBy<VisitItem> { it.date }.thenBy {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.hour)?.time ?: 0
                            }
                        )
                        loadingBar.visibility = View.GONE
                        if (visits.isEmpty()) {
                            emptyText.visibility = View.VISIBLE
                        } else {
                            emptyText.visibility = View.GONE
                        }
                        adapter.updateAppointments(visits)
                    }
            }
            .addOnFailureListener {
                loadingBar.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
            }
    }

    private fun confirmAndCancelAppointment(docId: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes") { _, _ -> cancelAppointment(docId) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelAppointment(docId: String) {
        db.collection("confirmedAppointments").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                loadAppointments()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
            }
    }
}
