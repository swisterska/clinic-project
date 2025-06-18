package com.example.eclinic.calendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.eclinic.R
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.SimpleDateFormat
import java.util.*

class MainCalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var firestore: FirebaseFirestore
    private var doctorId: String? = null
    private var visitName: String? = null
    private var visitPrice: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_calendar)

        calendarView = findViewById(R.id.calendarView)
        firestore = FirebaseFirestore.getInstance()

        doctorId = intent.getStringExtra("id")
        visitName = intent.getStringExtra("visitName")
        visitPrice = intent.getStringExtra("visitPrice")

        if (doctorId == null) {
            Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Prevent past date selection
        calendarView.state().edit()
            .setMinimumDate(CalendarDay.today())
            .commit()

        // Fetch available slots and highlight those days
        fetchAvailableDatesAndHighlight()

        // Handle date click
        calendarView.setOnDateChangedListener { _, selectedDate, _ ->
            val selectedMillis = Calendar.getInstance().apply {
                set(selectedDate.year, selectedDate.month, selectedDate.day)
            }.timeInMillis

            val nextIntent = if (visitName != null) {
                Intent(this, WeeklyScheduleActivityPatient::class.java).apply {
                    putExtra("visitName", visitName)
                    putExtra("visitPrice", visitPrice)
                }
            } else {
                Intent(this, WeeklyScheduleActivityDoctor::class.java)
            }

            nextIntent.putExtra("selectedDate", selectedMillis)
            nextIntent.putExtra("id", doctorId)
            startActivity(nextIntent)
        }
    }

    private fun fetchAvailableDatesAndHighlight() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())

        val appointmentsRef = firestore.collection("appointments").document(doctorId!!)
        val confirmedRef = firestore.collection("confirmedAppointments")

        appointmentsRef.get().addOnSuccessListener { doc ->
            val appointmentsData = doc.data.orEmpty()

            // Load all confirmed appointments for this doctor
            confirmedRef.whereEqualTo("doctorId", doctorId).get()
                .addOnSuccessListener { confirmedDocs ->

                    val confirmedMap = mutableMapOf<String, MutableList<String>>()

                    for (booking in confirmedDocs) {
                        val date = booking.getString("date") ?: continue
                        val hour = booking.getString("hour") ?: continue
                        confirmedMap.getOrPut(date) { mutableListOf() }.add(hour)
                    }

                    val greenDates = mutableListOf<CalendarDay>()
                    val redDates = mutableListOf<CalendarDay>()

                    for ((key, value) in appointmentsData) {
                        val slots = (value as? List<*>)?.filterIsInstance<String>() ?: continue
                        val date = formatter.parse(key) ?: continue

                        if (key >= today) {
                            val bookedSlots = confirmedMap[key] ?: emptyList()
                            val availableSlots = slots - bookedSlots

                            if (availableSlots.isEmpty()) {
                                redDates.add(CalendarDay.from(date))
                            } else {
                                greenDates.add(CalendarDay.from(date))
                            }
                        }
                    }

                    // Apply green dot decorator
                    calendarView.addDecorator(object : DayViewDecorator {
                        override fun shouldDecorate(day: CalendarDay): Boolean {
                            return greenDates.contains(day)
                        }

                        override fun decorate(view: DayViewFacade) {
                            view.addSpan(DotSpan(10f, ContextCompat.getColor(this@MainCalendarActivity, R.color.teal_200)))
                        }
                    })

                    // Apply red dot decorator
                    calendarView.addDecorator(object : DayViewDecorator {
                        override fun shouldDecorate(day: CalendarDay): Boolean {
                            return redDates.contains(day)
                        }

                        override fun decorate(view: DayViewFacade) {
                            view.addSpan(DotSpan(10f, ContextCompat.getColor(this@MainCalendarActivity, R.color.red)))
                        }
                    })
                }
        }
    }

}
