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

class DoctorAdapter(
    private val doctorList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit,
    private val onInfoClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.doctor_image)
        val name: TextView = view.findViewById(R.id.doctor_name)
        val bio: TextView = view.findViewById(R.id.doctor_bio)
        val description: TextView = view.findViewById(R.id.doctor_description)
        val infoIcon: ImageView = view.findViewById(R.id.info_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorList[position]

        holder.name.text = "${doctor.firstName} ${doctor.lastName}"
        holder.bio.text = doctor.bio
        holder.description.text = doctor.specialization

        holder.profilePic.setImageResource(R.drawable.default_image)

        holder.itemView.setOnClickListener {
            onDoctorClick(doctor)
        }

        holder.infoIcon.setOnClickListener {
            onInfoClick(doctor)
        }
    }

    override fun getItemCount(): Int = doctorList.size
}