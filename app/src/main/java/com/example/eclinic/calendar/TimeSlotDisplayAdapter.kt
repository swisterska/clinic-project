package com.example.eclinic.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * An adapter for displaying time slots in a [RecyclerView]. This adapter is versatile,
 * supporting both patient and doctor views for time slot selection and management.
 * It can highlight selected slots, booked slots, and handle click events for both.
 *
 * @param selectedSlots A list of available time slots to display.
 * @param selectedSlot The single time slot currently selected by a patient (optional).
 * @param onSlotSelected A callback function invoked when a patient selects a free time slot (optional).
 * @param bookedSlots A list of time slots that are already booked (for doctor's view, optional).
 * @param onBookedSlotClick A callback function invoked when a doctor clicks on a booked time slot (optional).
 */
class TimeSlotDisplayAdapter(
    private val selectedSlots: List<String>,
    private val selectedSlot: String? = null, // Patient selection
    private val onSlotSelected: ((String) -> Unit)? = null, // Patient click callback
    private val bookedSlots: List<String> = emptyList(), // Booked slots (doctor view)
    private val onBookedSlotClick: ((String) -> Unit)? = null // Doctor clicks red slot
) : RecyclerView.Adapter<TimeSlotDisplayAdapter.ViewHolder>() {

    /**
     * Provides a reference to the views for each time slot item.
     * @param view The root view of the time slot item layout.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textSlot: TextView = view.findViewById(R.id.textSlot)
    }

    /**
     * Called when [RecyclerView] needs a new [ViewHolder] of the given type to represent an item.
     * @param parent The [ViewGroup] into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [ViewHolder] that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by [RecyclerView] to display the data at the specified position.
     * This method updates the contents of the [ViewHolder.itemView] to reflect the item at the given position.
     * It handles different display logic based on whether the slot is booked (for doctors)
     * or selected (for patients).
     * @param holder The [ViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeSlot = selectedSlots[position]
        holder.textSlot.text = timeSlot

        if (bookedSlots.contains(timeSlot)) {
            // Booked slot (for Doctor) - red and clickable for info
            holder.itemView.setBackgroundColor(Color.RED)
            holder.itemView.setOnClickListener {
                onBookedSlotClick?.invoke(timeSlot) // Doctor clicks booked slot
            }
        } else {
            // Free slot (for Patient)
            if (timeSlot == selectedSlot) {
                holder.itemView.setBackgroundResource(R.drawable.selected_slot_background)
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            holder.itemView.setOnClickListener {
                onSlotSelected?.invoke(timeSlot) // Patient clicks free slot
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = selectedSlots.size
}