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
 * RecyclerView Adapter for displaying a list of doctors in the admin panel.
 * This adapter handles the layout and interaction for each doctor item,
 * providing options for clicking on a doctor's profile and optionally deleting it.
 *
 * @property doctorList The list of [Doctor] objects to be displayed.
 * @property onDoctorClick A lambda function to be invoked when a doctor item is clicked,
 * passing the [Doctor] object.
 * @property onDeleteClick An optional lambda function to be invoked when the delete button
 * for a doctor item is clicked, passing the [Doctor] object.
 */
class DoctorAdapterAdmin(
    private val doctorList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit,
    private val onDeleteClick: ((Doctor) -> Unit)? = null
) : RecyclerView.Adapter<DoctorAdapterAdmin.DoctorViewHolder>() {

    /**
     * ViewHolder for individual doctor items in the RecyclerView.
     * It holds the references to the UI elements of each item.
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
     * Called when RecyclerView needs a new [DoctorViewHolder] of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [DoctorViewHolder] that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_admin, parent, false)
        return DoctorViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [DoctorViewHolder.itemView] to reflect the doctor
     * at the given position.
     *
     * @param holder The [DoctorViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
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
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = doctorList.size
}