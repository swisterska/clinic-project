package com.example.eclinic.doctorClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.patientClasses.VisitItem

class TodayVisitsAdapter(private val visits: List<VisitItem>) :
    RecyclerView.Adapter<TodayVisitsAdapter.VisitViewHolder>() {

    class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvPatientName: TextView = view.findViewById(R.id.tvPatientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_visit, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.tvHour.text = visit.hour
        holder.tvType.text = visit.type
        holder.tvPatientName.text = visit.doctorName //tymczasowo
    }

    override fun getItemCount(): Int = visits.size
}
