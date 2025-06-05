package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Prescription
import java.text.SimpleDateFormat
import java.util.Locale

class PrescriptionAdapter(
    private val prescriptions: List<Prescription>,
    private val onClick: (Prescription) -> Unit
) : RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicationName: TextView = itemView.findViewById(R.id.medicationName)
        val date: TextView = itemView.findViewById(R.id.prescriptionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prescription, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prescription = prescriptions[position]
        holder.medicationName.text = "Prescription #${position + 1}"
        holder.date.text = prescription.timestamp?.toDate()?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
        } ?: "Unknown date"

        holder.itemView.setOnClickListener {
            onClick(prescription)
        }
    }

    override fun getItemCount(): Int = prescriptions.size
}
