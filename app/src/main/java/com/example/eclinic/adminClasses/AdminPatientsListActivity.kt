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
import com.example.eclinic.adminClasses.PatientAdapterAdmin
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for administrators to view, edit, and delete patient profiles.
 * This activity displays a list of patients fetched from Firestore and provides
 * functionalities to manage their information.
 */
class AdminPatientsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var patientAdapter: PatientAdapterAdmin
    private lateinit var noResultsText: TextView

    private val patientList = mutableListOf<Patient>()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the RecyclerView, and fetches the patient data.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].  Otherwise it is null.
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
        recyclerView.adapter = patientAdapter

        fetchPatients()
    }

    /**
     * Fetches the list of patients from the Firestore database.
     * Only users with the role "PATIENT" are retrieved.
     * The fetched data updates the [patientList] and notifies the [patientAdapter].
     * Displays a message if no patients are found or a Toast on failure.
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
     * Displays an AlertDialog to edit the details of a selected patient.
     * The dialog pre-fills the patient's current first name, last name, and email.
     * @param patient The [Patient] object whose details are to be edited.
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
     * Saves the changes made to a patient's profile to the Firestore database.
     * @param patient The [Patient] object with the updated information.
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
     * Opens the [EditProfileActivity] for a given patient.
     * This method is currently not directly used in the provided code but could be for a separate edit flow.
     * @param patient The [Patient] object whose profile is to be edited.
     */
    private fun openEditPatientProfile(patient: Patient) {
        val intent = Intent(this, EditProfileActivity::class.java).apply {
            putExtra("patientId", patient.uid)
        }
        startActivity(intent)
    }

    /**
     * Displays an AlertDialog to confirm the deletion of a patient's profile.
     * @param patient The [Patient] object to be deleted.
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
     * Deletes a patient's profile from the Firestore database.
     * @param patient The [Patient] object to be deleted.
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