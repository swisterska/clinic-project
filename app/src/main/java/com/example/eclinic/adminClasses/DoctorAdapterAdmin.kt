package com.example.eclinic.adminClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.doctorClasses.Doctor

/**
 * RecyclerView adapter for displaying a list of doctors in the admin panel.
 *
 * Allows the admin to view basic information about doctors, click to edit,
 * and optionally delete them.
 *
 * @param doctorList List of doctors to display.
 * @param onDoctorClick Callback triggered when a doctor item is clicked.
 * @param onDeleteClick Optional callback triggered when the delete button is clicked.
 */
class DoctorAdapterAdmin(
    private val doctorList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit,
    private val onDeleteClick: ((Doctor) -> Unit)? = null
) : RecyclerView.Adapter<DoctorAdapterAdmin.DoctorViewHolder>() {

    /**
     * ViewHolder class that holds the views for each doctor item.
     *
     * @param view The root view of the doctor item layout.
     */
    class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.doctor_image)
        val name: TextView = view.findViewById(R.id.doctor_name)
        val bio: TextView = view.findViewById(R.id.doctor_bio)
        val description: TextView = view.findViewById(R.id.doctor_description)
        val deleteIcon: Button = view.findViewById(R.id.delete_button)
    }

    /**
     * Inflates the layout for each doctor item and returns a new ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_admin, parent, false)
        return DoctorViewHolder(view)
    }

    /**
     * Binds data from a [Doctor] object to the corresponding views in the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorList[position]

        holder.name.text = "${doctor.firstName} ${doctor.lastName}"
        holder.bio.text = doctor.bio
        holder.description.text = doctor.specialization

        holder.itemView.setOnClickListener {
            onDoctorClick(doctor)
        }

        holder.deleteIcon?.setOnClickListener {
            onDeleteClick?.invoke(doctor)
        }
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int = doctorList.size
}
