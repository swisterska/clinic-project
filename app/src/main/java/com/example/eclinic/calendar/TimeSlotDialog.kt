package com.example.eclinic.calendar

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * A [DialogFragment] that displays a list of time slots for selection.
 * This dialog allows users to choose multiple time slots from a predefined list.
 * It uses a [RecyclerView] to display the time slots and a [TimeSlotAdapter] to manage selections.
 */
class TimeSlotDialog : DialogFragment() {

    /**
     * Interface for communicating selected time slots back to the hosting activity or fragment.
     */
    interface TimeSlotListener {
        /**
         * Called when the user confirms their selection of time slots.
         * @param slots A [List] of selected time slot strings (e.g., "09:00", "09:30").
         */
        fun onSlotsSelected(slots: List<String>)
    }

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private var initialSelection: List<String> = emptyList()

    /**
     * Called when the fragment is first created.
     * Retrieves any initial selected slots passed as arguments.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSelection = arguments?.getStringArrayList(ARG_SLOTS)?.toList() ?: emptyList()
    }

    /**
     * Creates and returns a new [Dialog] instance for the fragment.
     * This method inflates the dialog's layout, initializes the [RecyclerView] and its adapter,
     * and sets up the positive and negative buttons.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     * @return A new [Dialog] instance to be displayed by the fragment.
     */
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

    /**
     * Generates a list of time slots from 08:00 to 17:30 with 30-minute intervals.
     * @return A [List] of [String] representing the generated time slots (e.g., "08:00", "08:30").
     */
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

        /**
         * Creates a new instance of [TimeSlotDialog] with an initial selection of time slots.
         * @param selectedSlots A [List] of [String] representing the time slots that should be initially selected.
         * @return A new instance of [TimeSlotDialog].
         */
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