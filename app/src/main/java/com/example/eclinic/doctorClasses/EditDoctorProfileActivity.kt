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
 * Activity to edit and update the doctor's profile details.
 *
 * Allows the doctor to view and modify their personal information,
 * such as name, email, phone, specialization, title, workplace, bio, and PWZ number.
 * Also provides navigation to change password and to edit offered visit types.
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
     * Called when the activity is starting.
     *
     * Initializes UI elements, fetches current doctor's data from Firestore,
     * populates the form fields, and sets click listeners for saving changes,
     * editing offered services, and changing the password.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied. Otherwise it is null.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_doctor_profile)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Bind views
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

        // Open Change Password activity on button click
        changePasswordButton.setOnClickListener {
            val intent = Intent(this, ChangeDoctorPasswordActivity::class.java)
            startActivity(intent)
        }

        val userId = auth.currentUser?.uid

        // Load the current doctor's profile data from Firestore
        if (userId != null) {
            val docRef = firestore.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    firstNameEditText.setText(document.getString("firstName"))
                    lastNameEditText.setText(document.getString("lastName"))
                    emailEditText.setText(document.getString("email"))
                    phoneEditText.setText(document.getString("phoneNumber"))
                    specializationEditText.setText(document.getString("specialization"))
                    pwzEditText.setText(document.getString("pwz"))
                    titleEditText.setText(document.getString("title"))
                    workplaceEditText.setText(document.getString("workplace"))
                    bioEditText.setText(document.getString("bio"))
                }
            }
        }

        // Save updated profile data to Firestore
        saveButton.setOnClickListener {
            val updatedData = hashMapOf(
                "firstName" to firstNameEditText.text.toString(),
                "lastName" to lastNameEditText.text.toString(),
                "email" to emailEditText.text.toString(),
                "phoneNumber" to phoneEditText.text.toString(),
                "specialization" to specializationEditText.text.toString(),
                "pwz" to pwzEditText.text.toString(),
                "title" to titleEditText.text.toString(),
                "workplace" to workplaceEditText.text.toString(),
                "bio" to bioEditText.text.toString()
            )

            if (userId != null) {
                firestore.collection("users").document(userId)
                    .set(updatedData, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        finish() // Close activity after saving
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Navigate to visit types editing screen
        editOfferButton.setOnClickListener {
            val intent = Intent(this, DoctorVisitTypesActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}
