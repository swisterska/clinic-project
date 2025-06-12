package com.example.eclinic.adminClasses

import com.example.eclinic.patientClasses.Patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class PatientAdapterAdmin(
    private val patientList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit,
    private val onDeleteClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapterAdmin.PatientViewHolder>() {

    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.patient_image)
        val patientName: TextView = view.findViewById(R.id.patient_name)
        val patientEmail: TextView = view.findViewById(R.id.patient_email)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_admin, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]

        holder.patientName.text = "${patient.firstName} ${patient.lastName}"
        holder.patientEmail.text = patient.email
        holder.profilePic.setImageResource(R.drawable.default_patient)

        // Item click triggers edit
        holder.itemView.setOnClickListener {
            onPatientClick(patient)
        }

        // Delete button triggers delete callback
        holder.deleteButton.setOnClickListener {
            onDeleteClick(patient)
        }
    }

    override fun getItemCount(): Int = patientList.size
}
