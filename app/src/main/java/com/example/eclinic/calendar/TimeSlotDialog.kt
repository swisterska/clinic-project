package com.example.eclinic.calendar

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class TimeSlotDialog : DialogFragment() {

    interface TimeSlotListener {
        fun onSlotsSelected(slots: List<String>)
    }

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private var initialSelection: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSelection = arguments?.getStringArrayList(ARG_SLOTS)?.toList() ?: emptyList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_time_slots, null)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerTimeSlots)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val timeSlots = generateTimeSlots()
        timeSlotAdapter = TimeSlotAdapter(timeSlots, initialSelection.toMutableSet())
        recyclerView.adapter = timeSlotAdapter

        builder.setView(view)
            .setTitle("Select Time Slots")
            .setPositiveButton("Confirm") { _, _ ->
                val selected = timeSlotAdapter.getSelectedSlots()
                (activity as? TimeSlotListener)?.onSlotsSelected(selected)
            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }

    private fun generateTimeSlots(): List<String> {
        val slots = mutableListOf<String>()
        var hour = 8
        var minute = 0

        while (hour < 18 || (hour == 17 && minute <= 30)) {
            slots.add(String.format("%02d:%02d", hour, minute))
            minute += 30
            if (minute == 60) {
                minute = 0
                hour += 1
            }
        }

        return slots
    }

    companion object {
        private const val ARG_SLOTS = "selected_slots"

        fun newInstance(selectedSlots: List<String>): TimeSlotDialog {
            val args = Bundle().apply {
                putStringArrayList(ARG_SLOTS, ArrayList(selectedSlots))
            }
            return TimeSlotDialog().apply {
                arguments = args
            }
        }
    }
}
