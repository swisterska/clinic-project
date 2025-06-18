package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentsAdapter(
    private val appointments: MutableList<VisitItem>,
    private val onCancelClick: (VisitItem) -> Unit
) : RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val visitType: TextView = itemView.findViewById(R.id.textVisitType)
        val doctorName: TextView = itemView.findViewById(R.id.textDoctorName)
        val visitDate: TextView = itemView.findViewById(R.id.textVisitDate)
        val visitTime: TextView = itemView.findViewById(R.id.textVisitTime)
        val cancelBtn: Button = itemView.findViewById(R.id.cancelAppointmentBtn)
        val visitPrice: TextView = itemView.findViewById(R.id.textVisitPrice)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = appointments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.visitType.text = appointment.type
        holder.doctorName.text = appointment.doctorName
        holder.visitDate.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(appointment.date)
        holder.visitTime.text = appointment.hour
        holder.visitPrice.text = appointment.price


        holder.cancelBtn.setOnClickListener {
            onCancelClick(appointment)
        }
    }
    fun updateAppointments(newAppointments: List<VisitItem>) {
        appointments.clear()
        appointments.addAll(newAppointments)
        notifyDataSetChanged()
    }
}
