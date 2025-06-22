package com.example.eclinic.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * RecyclerView Adapter for displaying a list of selectable time slots.
 *
 * @param timeSlots List of available time slot strings (e.g., "09:00", "10:00").
 * @param selected A mutable set of currently selected time slots.
 */
class TimeSlotAdapter(
    private val timeSlots: List<String>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    /**
     * Creates a new ViewHolder by inflating the time slot item layout.
     *
     * @param parent The parent view group.
     * @param viewType The type of the view (not used here).
     * @return A SlotViewHolder for the time slot.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    /**
     * Binds a time slot to a ViewHolder.
     * Updates selection state and handles click events.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val time = timeSlots[position]
        holder.text.text = time
        holder.text.isSelected = selected.contains(time) // let the selector handle background

        holder.text.setOnClickListener {
            if (selected.contains(time)) {
                selected.remove(time)
            } else {
                selected.add(time)
            }
            notifyItemChanged(position)
        }
    }

    /**
     * Returns the total number of time slots.
     *
     * @return The size of the timeSlots list.
     */
    override fun getItemCount() = timeSlots.size

    /**
     * Returns the currently selected time slots as a list.
     *
     * @return List of selected time slot strings.
     */
    fun getSelectedSlots(): List<String> = selected.toList()

    /**
     * ViewHolder class for holding views for a single time slot item.
     *
     * @param view The view associated with the time slot item.
     */
    class SlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textSlot)
    }
}
