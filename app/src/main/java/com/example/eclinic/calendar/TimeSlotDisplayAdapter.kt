package com.example.eclinic.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class TimeSlotDisplayAdapter(private val selectedSlots: List<String>) :
    RecyclerView.Adapter<TimeSlotDisplayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textSlot.text = selectedSlots[position]
    }

    override fun getItemCount() = selectedSlots.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textSlot: TextView = view.findViewById(R.id.textSlot)
    }
}
