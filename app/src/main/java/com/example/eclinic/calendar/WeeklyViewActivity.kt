package com.example.eclinic.calendar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.calendar.CalendarUtils.daysInWeekArray
import com.example.eclinic.calendar.CalendarUtils.monthYearFromDate
import java.time.LocalDate
import android.widget.Button
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException


class WeeklyViewActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_view)
        initWidgets()

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

        // Ensures that days is never null
        val days = CalendarUtils.selectedDate?.let { daysInWeekArray(it) } ?: ArrayList<LocalDate?>()

        val calendarAdapter = CalendarAdapter(days, this, CalendarUtils.selectedDate)
        val layoutManager = GridLayoutManager(applicationContext, 7)
        calendarRecyclerView.layoutManager = layoutManager
        calendarRecyclerView.adapter = calendarAdapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun previousWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate?.minusWeeks(1)
        setWeekView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate?.plusWeeks(1)
        setWeekView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int, date: LocalDate?) {
        date?.let {
            CalendarUtils.selectedDate = it
            setWeekView()
            loadAppointmentSlots(date)
        }
    }
    private fun loadAppointmentSlots(date: LocalDate) {
        val slotLayout = findViewById<LinearLayout>(R.id.timeSlotsLayout)
        slotLayout.removeAllViews()

        val db = Firebase.firestore
        val auth = Firebase.auth

        //TODO: trzeba wrzucic z poprzedniego view wybranego lekarza
        val doctorId = intent.getStringExtra("doctorId") ?: return

        db.collection("appointments")
            .document(doctorId)
            .collection(date.toString())
            .document("slots")
            .collection("times")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val time = doc.id
                    val status = doc.getString("status") ?: "available"

                    val button = Button(this)
                    button.text = time
                    button.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    if (status == "taken") {
                        button.isEnabled = false
                        button.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                    } else {

                        //TODO: jak juz zrobimy ConfirmVisitView, to to całe transaction wrzuci się tam
                        button.setOnClickListener {
                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                Toast.makeText(this, "Please log in to book", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            // Firestore transaction to ensure slot is available when booking
                            val slotRef = db.collection("appointments")
                                .document(doctorId)
                                .collection(date.toString())
                                .document("slots")
                                .collection("times")
                                .document(time)

                            Firebase.firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(slotRef)
                                val status = snapshot.getString("status")

                                if (status == "taken") {
                                    // If the slot is already taken, cancel the transaction
                                    throw FirebaseFirestoreException("Slot already taken", FirebaseFirestoreException.Code.ABORTED)
                                } else {
                                    // If the slot is available, mark it as taken and save the user's ID
                                    transaction.update(slotRef, mapOf("status" to "taken", "bookedBy" to userId))

                                    // Save the booking info to the user's appointments collection
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
                                // Successfully booked
                                Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show()
                                loadAppointmentSlots(date) // Refresh UI to show updated status
                            }.addOnFailureListener { exception ->
                                if (exception is FirebaseFirestoreException && exception.code == FirebaseFirestoreException.Code.ABORTED) {
                                    // Handle slot already taken error
                                    Toast.makeText(this, "Sorry, this slot was taken while you were booking.", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Handle other errors
                                    Toast.makeText(this, "Failed to book slot: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }

                    slotLayout.addView(button)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load timeslots", Toast.LENGTH_SHORT).show()
            }
    }

    

    fun monthlyViewAction(view: View) {
        val intent = Intent(this, MainCalendarActivity::class.java)
        intent.putExtra("selectedDate", CalendarUtils.selectedDate.toString())
        startActivity(intent)
        finish() // prevents stacking activities
    }

}