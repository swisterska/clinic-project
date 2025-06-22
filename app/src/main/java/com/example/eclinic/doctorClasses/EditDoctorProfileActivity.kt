package com.example.eclinic.doctorClasses

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Activity for doctors to edit their profile information.
 * Doctors can update their personal details, professional qualifications, and workplace information.
 * It also provides links to manage their offered visit types and change their password.
 */
class EditDoctorProfileActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var specializationEditText: EditText
    private lateinit var pwzEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var workplaceEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var editOfferButton: Button
    private lateinit var changePasswordButton: Button

    /**
     * Called when the activity is first created.
     * Initializes UI components, Firebase instances, loads the doctor's current profile data,
     * and sets up click listeners for saving changes, editing visit types, and changing password.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    @SuppressLint("MissingInflatedId") // Suppresses a lint warning, ensure all IDs are correctly linked in XML
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_doctor_profile)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize EditText fields
        firstNameEditText = findViewById(R.id.editDoctorFirstName)
        lastNameEditText = findViewById(R.id.editDoctorLastName)
        emailEditText = findViewById(R.id.editDoctorEmail)
        phoneEditText = findViewById(R.id.editDoctorPhone)
        specializationEditText = findViewById(R.id.editDoctorSpecialization)
        pwzEditText = findViewById(R.id.editDoctorPwz)
        titleEditText = findViewById(R.id.editDoctorTitle)
        workplaceEditText = findViewById(R.id.editDoctorWorkplace)
        bioEditText = findViewById(R.id.editDoctorBio)
        saveButton = findViewById(R.id.saveDoctorProfileButton)
        editOfferButton = findViewById(R.id.EditYourServicesButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        // Set up listener for the "Change Password" button
        changePasswordButton.setOnClickListener {
            val intent = Intent(this, ChangeDoctorPasswordActivity::class.java)
            startActivity(intent)
        }

        val userId = auth.currentUser?.uid // Get the current authenticated user's ID

        // Load existing doctor profile data from Firestore
        if (userId != null) {
            val docRef = firestore.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Populate EditText fields with current profile data
                    firstNameEditText.setText(document.getString("firstName"))
                    lastNameEditText.setText(document.getString("lastName"))
                    emailEditText.setText(document.getString("email"))
                    phoneEditText.setText(document.getString("phoneNumber")) // Assuming "phoneNumber" field in Firestore
                    specializationEditText.setText(document.getString("specialization"))
                    pwzEditText.setText(document.getString("pwz"))
                    titleEditText.setText(document.getString("title"))
                    workplaceEditText.setText(document.getString("workplace"))
                    bioEditText.setText(document.getString("bio"))
                } else {
                    // Log or show a message if the document doesn't exist
                    Toast.makeText(this, "Doctor profile data not found.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                // Log or show a message if data retrieval fails
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish() // Close activity if user is not logged in
        }

        // Set up listener for the "Save" button
        saveButton.setOnClickListener {
            // Create a HashMap with updated profile data
            val updatedData = hashMapOf(
                "firstName" to firstNameEditText.text.toString(),
                "lastName" to lastNameEditText.text.toString(),
                "email" to emailEditText.text.toString(),
                "phoneNumber" to phoneEditText.text.toString(), // Ensure this matches Firestore field name
                "specialization" to specializationEditText.text.toString(),
                "pwz" to pwzEditText.text.toString(),
                "title" to titleEditText.text.toString(),
                "workplace" to workplaceEditText.text.toString(),
                "bio" to bioEditText.text.toString()
            )

            // Update the document in Firestore
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .set(updatedData, SetOptions.merge()) // Use merge to update only specified fields
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to the previous activity (e.g., DoctorProfileActivity)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Set up listener for the "Edit Offer" button
        editOfferButton.setOnClickListener {
            val intent = Intent(this, DoctorVisitTypesActivity::class.java)
            intent.putExtra("userId", userId) // Pass the user ID if needed in the next activity
            startActivity(intent)
        }
    }
}