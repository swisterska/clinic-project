package com.example.eclinic.calendar

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainCalendarActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView

    private lateinit var calendarView: CalendarView
    private var doctorId: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_calendar)

        calendarView = findViewById(R.id.calendarView)

        doctorId = intent.getStringExtra("id")

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (doctorId == null) {
                Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
                return@setOnDateChangeListener
            }

            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
            }

            val selectedDateMillis = calendar.timeInMillis

            val visitName = intent.getStringExtra("visitName")

            val nextIntent = if (visitName != null) {
                Intent(this, WeeklyScheduleActivityPatient::class.java).apply {
                    putExtra("visitName", visitName)
                }
            } else {
                Intent(this, WeeklyScheduleActivityDoctor::class.java)
            }

            nextIntent.putExtra("selectedDate", selectedDateMillis)
            nextIntent.putExtra("id", doctorId)
            startActivity(nextIntent)

        }




    }
}
