package com.example.eclinic.calendar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.calendar.CalendarUtils.daysInWeekArray
import com.example.eclinic.calendar.CalendarUtils.monthYearFromDate
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import java.time.LocalDate

class WeeklyViewActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_view)

        initWidgets()

        // Get role and selected date
        userRole = intent.getStringExtra("userRole") ?: "PATIENT"
        val dateStr = intent.getStringExtra("selectedDate")
        CalendarUtils.selectedDate = dateStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

        setWeekView()
        loadAppointmentSlots(CalendarUtils.selectedDate!!)
    }

    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setWeekView() {
        monthYearText.text = CalendarUtils.selectedDate?.let { monthYearFromDate(it) }

        val days = CalendarUtils.selectedDate?.let { daysInWeekArray(it) } ?: ArrayList()
        val calendarAdapter = CalendarAdapter(days, this, CalendarUtils.selectedDate)
        calendarRecyclerView.layoutManager = GridLayoutManager(applicationContext, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun previousWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate?.minusWeeks(1)
        setWeekView()
        loadAppointmentSlots(CalendarUtils.selectedDate!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate?.plusWeeks(1)
        setWeekView()
        loadAppointmentSlots(CalendarUtils.selectedDate!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int, date: LocalDate?) {
        date?.let {
            CalendarUtils.selectedDate = it
            setWeekView()
            loadAppointmentSlots(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadAppointmentSlots(date: LocalDate) {
        val slotLayout = findViewById<LinearLayout>(R.id.timeSlotsLayout)
        slotLayout.removeAllViews()

        val db = Firebase.firestore
        val auth = Firebase.auth
        val doctorId = intent.getStringExtra("doctorId") ?: return

        val slotsRef = db.collection("appointments")
            .document(doctorId)
            .collection(date.toString())
            .document("slots")
            .collection("times")

        slotsRef.get().addOnSuccessListener { result ->
            for (doc in result) {
                val time = doc.id
                val status = doc.getString("status") ?: "available"

                val button = Button(this)
                button.text = time
                button.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                if (userRole == "DOCTOR") {
                    // Doctor view — just show availability
                    if (status == "taken") {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                        button.text = "$time (Taken)"
                    } else {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                        button.text = "$time (Available)"
                    }
                    button.isEnabled = false
                } else {
                    // Patient view — allow booking
                    if (status == "taken") {
                        button.isEnabled = false
                        button.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                    } else {
                        button.setOnClickListener {
                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                Toast.makeText(this, "Please log in to book", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            val slotDocRef = slotsRef.document(time)

                            Firebase.firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(slotDocRef)
                                if (snapshot.getString("status") == "taken") {
                                    throw FirebaseFirestoreException("Slot already taken", FirebaseFirestoreException.Code.ABORTED)
                                } else {
                                    transaction.update(slotDocRef, mapOf(
                                        "status" to "taken",
                                        "bookedBy" to userId
                                    ))

                                    val userAppointmentsRef = db.collection("users")
                                        .document(userId)
                                        .collection("appointments")
                                        .document()

                                    transaction.set(userAppointmentsRef, mapOf(
                                        "doctorId" to doctorId,
                                        "date" to date.toString(),
                                        "time" to time
                                    ))
                                }
                            }.addOnSuccessListener {
                                Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show()
                                loadAppointmentSlots(date)
                            }.addOnFailureListener { ex ->
                                if (ex is FirebaseFirestoreException && ex.code == FirebaseFirestoreException.Code.ABORTED) {
                                    Toast.makeText(this, "Slot already taken.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Error: ${ex.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                slotLayout.addView(button)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load time slots", Toast.LENGTH_SHORT).show()
        }
    }

    fun monthlyViewAction(view: View) {
        val intent = Intent(this, MainCalendarActivity::class.java)
        intent.putExtra("selectedDate", CalendarUtils.selectedDate.toString())
        intent.putExtra("userRole", userRole)
        intent.putExtra("doctorId", intent.getStringExtra("doctorId"))
        startActivity(intent)
        finish()
    }
}
