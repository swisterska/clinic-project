package com.example.eclinic.calendar

import android.R.attr.id
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore


class WeeklyScheduleActivity : AppCompatActivity(), TimeSlotDialog.TimeSlotListener {

    private lateinit var dateTitle: TextView
    private lateinit var btnAddSlots: Button
    private lateinit var recyclerView: RecyclerView


    private lateinit var doctorId: String
    private lateinit var selectedDate: Calendar
    private var selectedSlots: List<String> = listOf()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_schedule)

        doctorId = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Missing doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dateTitle = findViewById(R.id.dateTitle)
        btnAddSlots = findViewById(R.id.btnAddSlots)
        recyclerView = findViewById(R.id.recyclerView)

        val selectedDateMillis = intent.getLongExtra("selectedDate", 0L)
        selectedDate = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

        updateDateTitle()

        btnAddSlots.setOnClickListener {
            val dialog = TimeSlotDialog.newInstance(selectedSlots)
            dialog.show(supportFragmentManager, "TimeSlotDialog")
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        updateTimeSlots()

        selectedDate.timeInMillis = selectedDateMillis
        updateDateTitle()
        loadSlotsFromFirebase()
    }


    private fun updateDateTitle() {
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        dateTitle.text = formatter.format(selectedDate.time)
    }

    private fun updateTimeSlots() {
        val adapter = TimeSlotDisplayAdapter(selectedSlots)
        recyclerView.adapter = adapter
    }

    override fun onSlotsSelected(slots: List<String>) {
        selectedSlots = slots
        updateTimeSlots()
        saveSlotsToFirebase()
    }

    private fun saveSlotsToFirebase() {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        val data = mapOf(dateKey to selectedSlots)

        db.collection("appointments")
            .document(doctorId)
            .set(data, SetOptions.merge()) // merge to avoid overwriting other days
            .addOnSuccessListener {
                Toast.makeText(this, "Slots saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSlotsFromFirebase() {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        db.collection("appointments")
            .document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains(dateKey)) {
                    val slots = document[dateKey] as? List<*> ?: listOf<String>()
                    selectedSlots = slots.filterIsInstance<String>()
                    updateTimeSlots()
                }
            }
    }


}
