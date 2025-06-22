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
 * RecyclerView Adapter for displaying a list of patients to the admin.
 *
 * @param patientList List of Patient objects to display.
 * @param onPatientClick Callback invoked when a patient item is clicked.
 * @param onDeleteClick Callback invoked when the delete button is clicked for a patient.
 */
class PatientAdapterAdmin(
    private val patientList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit,
    private val onDeleteClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapterAdmin.PatientViewHolder>() {

    /**
     * ViewHolder class for holding views of a single patient item.
     *
     * @param view The inflated layout view for a patient item.
     */
    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.patient_image)
        val patientName: TextView = view.findViewById(R.id.patient_name)
        val patientEmail: TextView = view.findViewById(R.id.patient_email)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
    }

    /**
     * Inflates the layout for a single patient item and returns a ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new PatientViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_admin, parent, false)
        return PatientViewHolder(view)
    }

    /**
     * Binds the data of a patient to the corresponding views.
     *
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the patient item in the list.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The number of patient items.
     */
    override fun getItemCount(): Int = patientList.size
}
