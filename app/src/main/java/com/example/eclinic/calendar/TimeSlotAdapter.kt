package com.example.eclinic.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * An adapter for displaying a list of time slots in a [RecyclerView] and managing their selection state.
 * This adapter is designed to allow multiple time slots to be selected or deselected by the user.
 * The selection state is visually managed by a `selector` drawable on the TextView.
 *
 * @param timeSlots The list of all available time slot strings (e.g., "09:00", "09:30").
 * @param selected A [MutableSet] holding the time slot strings that are currently selected.
 */
class TimeSlotAdapter(
    private val timeSlots: List<String>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    /**
     * Called when [RecyclerView] needs a new [SlotViewHolder] of the given type to represent an item.
     * @param parent The [ViewGroup] into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [SlotViewHolder] that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    /**
     * Called by [RecyclerView] to display the data at the specified position.
     * This method updates the contents of the [SlotViewHolder.itemView] to reflect the item at the given position.
     * It sets the time slot text, updates its selection state, and attaches an [OnClickListener]
     * to toggle selection on click.
     * @param holder The [SlotViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val time = timeSlots[position]
        holder.text.text = time
        // Let the selector drawable handle the background based on the 'isSelected' state.
        holder.text.isSelected = selected.contains(time)

        holder.text.setOnClickListener {
            if (selected.contains(time)) {
                selected.remove(time)
            } else {
                selected.add(time)
            }
            // Notify the adapter that the item at this position has changed, triggering a re-bind.
            notifyItemChanged(position)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = timeSlots.size

    /**
     * Returns a [List] of the currently selected time slots.
     * @return A [List] of [String] representing the selected time slots.
     */
    fun getSelectedSlots(): List<String> = selected.toList()

    /**
     * Provides a reference to the views for each time slot item.
     * @param view The root view of the time slot item layout.
     */
    class SlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textSlot)
    }
}