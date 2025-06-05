package com.example.eclinic.calendar

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
import java.time.LocalTime

class WeeklyViewActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_view)

        userRole = intent.getStringExtra("userRole") ?: "PATIENT"
        val doctorId = intent.getStringExtra("id")



        initWidgets()

        val dateStr = intent.getStringExtra("selectedDate")
        CalendarUtils.selectedDate = dateStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

        val addSlotBtn = findViewById<Button>(R.id.addSlotBtn)
        addSlotBtn.setOnClickListener {
            CalendarUtils.selectedDate?.let { date ->
                val timeSlots = generateHalfHourSlots()
                showMultiSlotDialog(timeSlots, date)

            }

        }

        setWeekView()
        CalendarUtils.selectedDate?.let { loadAppointmentSlots(it) }
    }

    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateHalfHourSlots(): List<String> {
        val slots = mutableListOf<String>()
        var time = LocalTime.of(8, 0)
        val endTime = LocalTime.of(18, 0)
        while (time.isBefore(endTime)) {
            slots.add(time.toString())
            time = time.plusMinutes(30)
        }
        return slots
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
        CalendarUtils.selectedDate?.let { loadAppointmentSlots(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate?.plusWeeks(1)
        setWeekView()
        CalendarUtils.selectedDate?.let { loadAppointmentSlots(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int, date: LocalDate?) {
        date?.let {
            CalendarUtils.selectedDate = it
            setWeekView()
            loadAppointmentSlots(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadAppointmentSlots(date: LocalDate) {
        val db = Firebase.firestore
        val auth = Firebase.auth
        val doctorId = intent.getStringExtra("id") ?: return

        val addSlotBtn = findViewById<Button>(R.id.addSlotBtn)
        val selectedSlotsText = findViewById<TextView>(R.id.selectedSlotsText)

        val slotsRef = db.collection("appointments")
            .document(doctorId)
            .collection(date.toString())
            .document("slots")
            .collection("times")

        val timeSlots = generateHalfHourSlots()

        // Show Add Slot button for doctors only
        if (userRole == "DOCTOR") {
            addSlotBtn.visibility = View.VISIBLE


        } else {
            addSlotBtn.visibility = View.GONE
        }

        slotsRef.get().addOnSuccessListener { result ->
            val existingSlots = result.documents.associateBy { it.id }

            if (userRole == "DOCTOR") {
                // Show the TextView for doctors
                selectedSlotsText.visibility = View.VISIBLE

                val availableSlots = existingSlots.filter { it.value.getString("status") == "available" }.keys
                selectedSlotsText.text = if (availableSlots.isEmpty())
                    "No available time slots." else "Available slots: ${availableSlots.joinToString(", ")}"

                // Optionally hide slot buttons for doctors
                findViewById<LinearLayout>(R.id.timeSlotsLayout)?.visibility = View.GONE

            } else if (userRole == "PATIENT") {
                // Hide the TextView for patients
                selectedSlotsText.visibility = View.GONE

                val slotLayout = findViewById<LinearLayout>(R.id.timeSlotsLayout)
                slotLayout?.visibility = View.VISIBLE
                slotLayout?.removeAllViews()

                var anyAvailable = false

                for (slot in timeSlots) {
                    val slotDoc = existingSlots[slot]
                    val status = slotDoc?.getString("status") ?: "none"

                    val button = Button(this).apply {
                        text = slot
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    when (status) {
                        "available" -> {
                            anyAvailable = true
                            button.setBackgroundColor(getColor(android.R.color.holo_green_light))
                            button.setOnClickListener {
                                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                                Firebase.firestore.runTransaction { transaction ->
                                    val snapshot = transaction.get(slotsRef.document(slot))
                                    if (snapshot.getString("status") == "taken") {
                                        throw FirebaseFirestoreException("Slot already taken", FirebaseFirestoreException.Code.ABORTED)
                                    }
                                    transaction.update(slotsRef.document(slot), mapOf(
                                        "status" to "taken",
                                        "bookedBy" to userId
                                    ))
                                    val userAppointmentsRef = db.collection("users")
                                        .document(userId)
                                        .collection("appointments")
                                        .document()
                                    transaction.set(userAppointmentsRef, mapOf(
                                        "id" to doctorId,
                                        "date" to date.toString(),
                                        "time" to slot
                                    ))
                                }.addOnSuccessListener {
                                    Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show()
                                    loadAppointmentSlots(date)
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        "taken" -> {
                            button.setBackgroundColor(getColor(android.R.color.holo_red_light))
                            button.text = "$slot (Taken)"
                            button.isEnabled = false
                        }
                        else -> {
                            button.setBackgroundColor(getColor(android.R.color.darker_gray))
                            button.isEnabled = false
                        }
                    }

                    slotLayout?.addView(button)
                }

                if (!anyAvailable && slotLayout != null) {
                    val noSlots = TextView(this).apply {
                        text = "No available time slots."
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        setTextColor(getColor(android.R.color.darker_gray))
                        setPadding(8, 16, 8, 16)
                    }
                    slotLayout.addView(noSlots)
                }
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Error loading slots: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun monthlyViewAction(view: View) {
        val doctorId = this.intent.getStringExtra("id")
        val intent = Intent(this, MainCalendarActivity::class.java)
        intent.putExtra("selectedDate", CalendarUtils.selectedDate.toString())
        intent.putExtra("userRole", userRole)
        intent.putExtra("id", doctorId)
        startActivity(intent)
        finish()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun openAddSlotDialog() {
        val slots = generateHalfHourSlots()
        showMultiSlotDialog(slots, CalendarUtils.selectedDate ?: return)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showMultiSlotDialog(allSlots: List<String>, date: LocalDate) {
        val selectedItems = mutableListOf<Int>()
        val selectedBooleans = BooleanArray(allSlots.size)

        AlertDialog.Builder(this)
            .setTitle("Select Available Time Slots")
            .setMultiChoiceItems(allSlots.toTypedArray(), selectedBooleans) { _, which, isChecked ->
                if (isChecked) selectedItems.add(which) else selectedItems.remove(which)
            }
            .setPositiveButton("Save") { _, _ ->
                val doctorId = intent.getStringExtra("doctorId") ?: return@setPositiveButton
                val db = Firebase.firestore
                val batch = db.batch()

                selectedItems.forEach { index ->
                    val slotTime = allSlots[index]
                    val slotId = "${date}_$slotTime"  // e.g., "2025-06-05_08:00"
                    val docRef = db.collection("appointments")
                        .document(doctorId)
                        .collection("slots")
                        .document(slotId)

                    Log.d("FirebasePathDebug", "Saving slot to: appointments/$doctorId/slots/$slotId")

                    batch.set(docRef, mapOf(
                        "status" to "available",
                        "date" to date.toString(),
                        "time" to slotTime
                    ))
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "Slots saved!", Toast.LENGTH_SHORT).show()
                    loadAppointmentSlots(date)
                }.addOnFailureListener {
                    Toast.makeText(this, "Error saving slots: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}