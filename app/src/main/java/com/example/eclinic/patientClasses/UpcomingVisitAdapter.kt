package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import java.text.SimpleDateFormat
import java.util.*

data class VisitItem(val date: Date, val hour: String, val type: String, val doctorName: String, val documentId: String, val price: String)

class UpcomingVisitAdapter(private val visits: List<VisitItem>) :
    RecyclerView.Adapter<UpcomingVisitAdapter.VisitViewHolder>() {

    class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeText: TextView = view.findViewById(R.id.textVisitType)
        val doctorNameText: TextView = view.findViewById(R.id.textDoctorName) // nowy
        val dateText: TextView = view.findViewById(R.id.textVisitDate)
        val timeText: TextView = view.findViewById(R.id.textVisitTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_visit, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        val dateFormatter = SimpleDateFormat("EEE, d MMMM yyyy", Locale.getDefault())

        holder.typeText.text = visit.type
        holder.doctorNameText.text = visit.doctorName
        holder.dateText.text = dateFormatter.format(visit.date)
        holder.timeText.text = visit.hour
    }

    override fun getItemCount() = visits.size
}
