package com.example.eclinic.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class TimeSlotAdapter(
    private val timeSlots: List<String>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

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

    override fun getItemCount() = timeSlots.size

    fun getSelectedSlots(): List<String> = selected.toList()

    class SlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textSlot)
    }
}
