package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Visit

class VisitAdapter(private val visits: List<Visit>) :
    RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {

    class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val visitName: TextView = view.findViewById(R.id.visit_name)
        val visitPrice: TextView = view.findViewById(R.id.visit_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visits, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.visitName.text = visit.name
        holder.visitPrice.text = visit.price
    }

    override fun getItemCount(): Int = visits.size
}
