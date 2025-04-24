package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eclinic.R
import com. example. eclinic. firebase. Doctor

class DoctorAdapter(private val doctorList: List<Doctor>) :
    RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.doctor_image)
        val name: TextView = view.findViewById(R.id.doctor_name)
        val bio: TextView = view.findViewById(R.id.doctor_bio)
        val description: TextView = view.findViewById(R.id.doctor_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorList[position]
        holder.name.text = doctor.name
        holder.bio.text = doctor.bio
        holder.description.text = doctor.description

        // Load doctor image using Glide
        Glide.with(holder.itemView.context)
            .load(doctor.profilePicUrl)
            .placeholder(R.drawable.default_image)
            .into(holder.profilePic)
    }

    override fun getItemCount(): Int = doctorList.size
}
