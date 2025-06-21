package com.example.eclinic.adminClasses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.doctorClasses.Doctor
import com.example.eclinic.doctorClasses.EditDoctorProfileActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminDoctorsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapterAdmin
    private lateinit var noResultsText: TextView

    private val doctorList = mutableListOf<Doctor>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_doctors_list)

        recyclerView = findViewById(R.id.recycler_view_doctors)
        noResultsText = findViewById(R.id.noResultsDoctors)

        recyclerView.layoutManager = LinearLayoutManager(this)
        doctorAdapter = DoctorAdapterAdmin(
            doctorList,
            onDoctorClick = { doctor -> showEditDoctorDialog(doctor) },
            onDeleteClick = { doctor -> confirmDeleteDoctor(doctor) }
        )
        recyclerView.adapter = doctorAdapter

        fetchDoctors()
    }


    private fun fetchDoctors() {
        db.collection("users")
            .whereEqualTo("role", "DOCTOR")
            .whereEqualTo("verified", true)
            .get()
            .addOnSuccessListener { documents ->
                doctorList.clear()
                for (doc in documents) {
                    val doctor = doc.toObject(Doctor::class.java)
                    doctorList.add(doctor)
                }
                doctorAdapter.notifyDataSetChanged()
                noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("AdminDoctorsList", "Error fetching doctors: ${e.message}")
                Toast.makeText(this, "Failed to load doctors", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showEditDoctorDialog(doctor: Doctor) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_doctor, null)
        val editFirstName = dialogView.findViewById<EditText>(R.id.editFirstName)
        val editLastName = dialogView.findViewById<EditText>(R.id.editLastName)
        val editBio = dialogView.findViewById<EditText>(R.id.editBio)

        // Pre-fill fields with current data
        editFirstName.setText(doctor.firstName)
        editLastName.setText(doctor.lastName)
        editBio.setText(doctor.bio)

        AlertDialog.Builder(this)
            .setTitle("Edit Doctor")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newFirstName = editFirstName.text.toString().trim()
                val newLastName = editLastName.text.toString().trim()
                val newBio = editBio.text.toString().trim()

                if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                    Toast.makeText(this, "First and last name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Update Firestore document
                val updates = hashMapOf<String, Any>(
                    "firstName" to newFirstName,
                    "lastName" to newLastName,
                    "bio" to newBio
                )

                doctor.uid?.let { uid ->
                    db.collection("users").document(uid)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Doctor info updated", Toast.LENGTH_SHORT).show()
                            fetchDoctors() // Refresh the list
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(this, "Invalid doctor ID", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditDoctorProfile(doctor: Doctor) {
        val intent = Intent(this, EditDoctorProfileActivity::class.java).apply {
            putExtra("doctorId", doctor.uid)
        }
        startActivity(intent)
    }

    private fun confirmDeleteDoctor(doctor: Doctor) {
        AlertDialog.Builder(this)
            .setTitle("Delete Doctor")
            .setMessage("Are you sure you want to delete Dr. ${doctor.firstName} ${doctor.lastName}'s profile?")
            .setPositiveButton("Delete") { _, _ -> deleteDoctor(doctor) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDoctor(doctor: Doctor) {
        if (doctor.uid == null) {
            Toast.makeText(this, "Invalid doctor ID", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("users").document(doctor.uid!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Doctor deleted", Toast.LENGTH_SHORT).show()
                fetchDoctors()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete doctor: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
