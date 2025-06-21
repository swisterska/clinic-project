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
import com.example.eclinic.patientClasses.EditProfileActivity
import com.example.eclinic.patientClasses.Patient
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity that allows administrators to view, edit, and delete patient accounts.
 *
 * Patients are fetched from the Firestore database and displayed in a RecyclerView.
 * Administrators can modify or delete patient information directly from this screen.
 */
class AdminPatientsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var patientAdapter: PatientAdapterAdmin
    private lateinit var noResultsText: TextView

    private val patientList = mutableListOf<Patient>()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Called when the activity is first created.
     * Initializes UI elements, sets up the RecyclerView and adapter,
     * and fetches patient data from Firestore.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_patients_list)

        recyclerView = findViewById(R.id.recycler_view_patients)
        noResultsText = findViewById(R.id.noResultsPatients)

        recyclerView.layoutManager = LinearLayoutManager(this)
        patientAdapter = PatientAdapterAdmin(
            patientList,
            onPatientClick = { patient -> showEditPatientDialog(patient) },
            onDeleteClick = { patient -> confirmDeletePatient(patient) }
        )
        recyclerView.adapter = patientAdapter

        fetchPatients()
    }

    /**
     * Fetches all users with the role "PATIENT" from Firestore.
     * Updates the RecyclerView with the fetched list.
     * Displays a message if no patients are found.
     */
    private fun fetchPatients() {
        db.collection("users")
            .whereEqualTo("role", "PATIENT")
            .get()
            .addOnSuccessListener { documents ->
                patientList.clear()
                for (doc in documents) {
                    val patient = doc.toObject(Patient::class.java)
                    patientList.add(patient)
                }
                patientAdapter.notifyDataSetChanged()
                noResultsText.visibility = if (patientList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("AdminPatientsList", "Error fetching patients: ${e.message}")
                Toast.makeText(this, "Failed to load patients", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Opens a dialog allowing the admin to edit a patient's details.
     *
     * @param patient The patient whose information is to be edited.
     */
    private fun showEditPatientDialog(patient: Patient) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_patient, null)
        val firstNameInput = dialogView.findViewById<EditText>(R.id.editFirstName)
        val lastNameInput = dialogView.findViewById<EditText>(R.id.editLastName)
        val emailInput = dialogView.findViewById<EditText>(R.id.editEmail)

        firstNameInput.setText(patient.firstName)
        lastNameInput.setText(patient.lastName)
        emailInput.setText(patient.email)

        AlertDialog.Builder(this)
            .setTitle("Edit Patient")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedPatient = patient.copy(
                    firstName = firstNameInput.text.toString(),
                    lastName = lastNameInput.text.toString(),
                    email = emailInput.text.toString()
                )
                savePatientChanges(updatedPatient)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Saves changes made to a patient's profile in Firestore.
     *
     * @param patient The updated patient data to be saved.
     */
    private fun savePatientChanges(patient: Patient) {
        if (patient.uid == null) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("users").document(patient.uid)
            .update(
                mapOf(
                    "firstName" to patient.firstName,
                    "lastName" to patient.lastName,
                    "email" to patient.email
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Patient updated", Toast.LENGTH_SHORT).show()
                fetchPatients()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update patient: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Launches the EditProfileActivity for the given patient.
     *
     * @param patient The patient whose profile is to be edited.
     */
    private fun openEditPatientProfile(patient: Patient) {
        val intent = Intent(this, EditProfileActivity::class.java).apply {
            putExtra("patientId", patient.uid)
        }
        startActivity(intent)
    }

    /**
     * Shows a confirmation dialog to delete the specified patient.
     *
     * @param patient The patient to confirm deletion for.
     */
    private fun confirmDeletePatient(patient: Patient) {
        AlertDialog.Builder(this)
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete ${patient.firstName} ${patient.lastName}'s profile?")
            .setPositiveButton("Delete") { _, _ -> deletePatient(patient) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes the specified patient from Firestore.
     *
     * @param patient The patient to be deleted.
     */
    private fun deletePatient(patient: Patient) {
        if (patient.uid == null) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("users").document(patient.uid!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show()
                fetchPatients()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete patient: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
