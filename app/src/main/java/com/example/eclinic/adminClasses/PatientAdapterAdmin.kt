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

/**
 * RecyclerView adapter for displaying a list of patients in the admin panel.
 *
 * Allows the admin to view basic patient information, select a patient to edit,
 * and delete a patient from the list.
 *
 * @param patientList List of patients to display.
 * @param onPatientClick Callback invoked when a patient item is clicked.
 * @param onDeleteClick Callback invoked when the delete button is clicked.
 */
class PatientAdapterAdmin(
    private val patientList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit,
    private val onDeleteClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapterAdmin.PatientViewHolder>() {

    /**
     * ViewHolder class holding the views for each patient item.
     *
     * @param view The root view of the patient item layout.
     */
    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.patient_image)
        val patientName: TextView = view.findViewById(R.id.patient_name)
        val patientEmail: TextView = view.findViewById(R.id.patient_email)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
    }

    /**
     * Inflates the layout for each patient item and returns a new ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_admin, parent, false)
        return PatientViewHolder(view)
    }

    /**
     * Binds patient data to the views in the ViewHolder for the given position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the patient in the list.
     */
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]

        holder.patientName.text = "${patient.firstName} ${patient.lastName}"
        holder.patientEmail.text = patient.email
        holder.profilePic.setImageResource(R.drawable.default_patient)

        // Clicking the entire item triggers the edit patient callback
        holder.itemView.setOnClickListener {
            onPatientClick(patient)
        }

        // Clicking the delete button triggers the delete callback
        holder.deleteButton.setOnClickListener {
            onDeleteClick(patient)
        }
    }

    /**
     * Returns the total number of patients in the list.
     */
    override fun getItemCount(): Int = patientList.size
}
