package com.example.eclinic.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class TimeSlotDisplayAdapter(
    private val selectedSlots: List<String>,
    private val selectedSlot: String? = null, // Patient selection
    private val onSlotSelected: ((String) -> Unit)? = null, // Patient click callback
    private val bookedSlots: List<String> = emptyList(), // Booked slots (doctor view)
    private val onBookedSlotClick: ((String) -> Unit)? = null // Doctor clicks red slot
) : RecyclerView.Adapter<TimeSlotDisplayAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textSlot: TextView = view.findViewById(R.id.textSlot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return ViewHolder(view)
    }

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

    override fun getItemCount() = selectedSlots.size
}
