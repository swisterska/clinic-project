package com.example.eclinic.patientClasses

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Doctor
import com.example.eclinic.firebase.Role
import com.example.eclinic.firebase.Specialization
import com.example.eclinic.firebase.User
import com.google.firebase.firestore.FirebaseFirestore

class RegisterForAppointmentPatient : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var specializationSpinner: Spinner
    private val doctorList = mutableListOf<Doctor>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_for_appointment_patient)

        recyclerView = findViewById(R.id.recyclerView)
        specializationSpinner = findViewById(R.id.specialization_spinner)

        recyclerView.layoutManager = LinearLayoutManager(this)
        doctorAdapter = DoctorAdapter(doctorList)
        recyclerView.adapter = doctorAdapter

        setupSpecializationFilter()
        fetchDoctorsFromFirestore(null) // Load all doctors initially
    }

    private fun setupSpecializationFilter() {
        val specializations = listOf("All") + Specialization.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = adapter

        specializationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSpecialization = if (position == 0) null else specializations[position]
                fetchDoctorsFromFirestore(selectedSpecialization)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchDoctorsFromFirestore(specialization: String?) {
        var query = db.collection("users").whereEqualTo("role", Role.DOCTOR.name)

        if (specialization != null) {
            query = query.whereEqualTo("specialization", specialization)
        }

        query.get()
            .addOnSuccessListener { documents ->
                doctorList.clear()
                for (doc in documents) {
                    val data = doc.data
                    val doctor = User.fromMap(data)

                    doctorList.add(Doctor(
                        name = "${doctor.firstName} ${doctor.lastName}",
                        bio = doctor.specialization,
                        description = "Experienced in ${doctor.specialization}",
                        profilePicUrl = doctor.profilePictureUrl
                    ))
                }
                doctorAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting doctors", e)
            }
    }
}
