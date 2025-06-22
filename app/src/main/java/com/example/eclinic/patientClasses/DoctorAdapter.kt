package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eclinic.R
import com.example.eclinic.doctorClasses.Doctor

/**
 * [DoctorAdapter] is a [RecyclerView.Adapter] that displays a list of [Doctor] objects.
 * Each item in the list shows the doctor's profile picture, name, bio, and specialization.
 * It also handles click events on the doctor's entire item and on a specific info icon.
 *
 * @param doctorList The list of [Doctor] objects to be displayed in the RecyclerView.
 * @param onDoctorClick A lambda function invoked when the main doctor item view is clicked.
 * It receives the [Doctor] object corresponding to the clicked item.
 * @param onInfoClick A lambda function invoked when the info icon for a doctor is clicked.
 * It receives the [Doctor] object corresponding to the clicked item.
 */
class DoctorAdapter(
    private val doctorList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit,
    private val onInfoClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    /**
     * [DoctorViewHolder] is a [RecyclerView.ViewHolder] that holds the views for a single doctor item.
     * It provides direct access to all the UI elements within a doctor list item.
     *
     * @param view The root [View] of the item layout (e.g., `item_doctor.xml`).
     */
    class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.doctor_image)
        val name: TextView = view.findViewById(R.id.doctor_name)
        val bio: TextView = view.findViewById(R.id.doctor_bio)
        val description: TextView = view.findViewById(R.id.doctor_description)
        val infoIcon: ImageView = view.findViewById(R.id.info_icon)
    }

    /**
     * Called when [RecyclerView] needs a new [DoctorViewHolder] of the given type to represent an item.
     * This method inflates the layout for a single doctor item from `item_doctor.xml`.
     *
     * @param parent The [ViewGroup] into which the new [View] will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new [View].
     * @return A new [DoctorViewHolder] that holds a [View] of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    /**
     * Called by [RecyclerView] to display the data at the specified `position`.
     * This method updates the contents of the [holder]'s [itemView] to reflect the doctor
     * item at the given `position`. It sets the doctor's name, bio, and specialization,
     * and attaches click listeners to the entire item and the info icon.
     *
     * @param holder The [DoctorViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorList[position]

        // Set doctor's name, bio, and specialization
        holder.name.text = "${doctor.firstName} ${doctor.lastName}"
        holder.bio.text = doctor.bio
        holder.description.text = doctor.specialization

        // Set the profile picture to a default image.
        // Note: The Glide library is imported but not currently used for loading images from URLs in this method.
        holder.profilePic.setImageResource(R.drawable.default_image)

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener {
            onDoctorClick(doctor)
        }

        // Set click listener for the info icon
        holder.infoIcon.setOnClickListener {
            onInfoClick(doctor)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = doctorList.size
}