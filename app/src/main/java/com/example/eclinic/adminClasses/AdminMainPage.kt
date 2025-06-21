package com.example.eclinic.adminClasses

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.eclinic.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Admin main page activity.
 * Allows the admin to view and verify doctors, add new doctors, and navigate to other admin features.
 */
class AdminMainPage : AppCompatActivity() {

    private lateinit var layoutContainer: LinearLayout
    private lateinit var btnAddDoctor: MaterialButton
    private lateinit var btnLogout: ImageButton
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Called when the activity is created.
     * Initializes UI components and sets up event listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main_page)

        layoutContainer = findViewById(R.id.layoutContainer)
        btnAddDoctor = findViewById(R.id.btnAddDoctor)
        btnLogout = findViewById(R.id.btnLogout)

        fetchUnverifiedDoctors()

        btnAddDoctor.setOnClickListener {
            showAddDoctorDialog()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, com.example.eclinic.logRegClasses.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_manage_doctors -> {
                    startActivity(Intent(this, AdminDoctorsListActivity::class.java))
                    true
                }
                R.id.menu_manage_schedules -> {
                    startActivity(Intent(this, AdminCalendarActivity::class.java))
                    true
                }
                R.id.menu_manage_patients -> {
                    startActivity(Intent(this, AdminPatientsListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Fetches all unverified doctors from Firestore and displays them in the layout container.
     */
    private fun fetchUnverifiedDoctors() {
        layoutContainer.removeAllViews()

        db.collection("users")
            .whereEqualTo("role", "DOCTOR")
            .whereEqualTo("verified", false)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val emptyText = TextView(this).apply {
                        text = "No unverified doctors found."
                        textSize = 16f
                        gravity = Gravity.CENTER
                    }
                    layoutContainer.addView(emptyText)
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val doctorId = doc.id
                    val name = doc.getString("firstName") ?: "Unknown"
                    val surname = doc.getString("lastName") ?: ""
                    val specialization = doc.getString("specialization") ?: "Not Specified"

                    val card = layoutInflater.inflate(R.layout.doctor_card_item, layoutContainer, false)

                    val doctorName = card.findViewById<TextView>(R.id.doctorName)
                    val doctorDetails = card.findViewById<TextView>(R.id.doctorDetails)
                    val btnVerify = card.findViewById<MaterialButton>(R.id.btnVerify)
                    val btnReject = card.findViewById<MaterialButton>(R.id.btnReject)

                    doctorName.text = "Dr. $name $surname"
                    doctorDetails.text = "Specialty: $specialization"

                    btnVerify.setOnClickListener { verifyDoctor(doctorId) }
                    btnReject.setOnClickListener { rejectDoctor(doctorId) }

                    layoutContainer.addView(card)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch unverified doctors.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Verifies a doctor by updating their status in Firestore.
     *
     * @param doctorId The ID of the doctor to verify.
     */
    private fun verifyDoctor(doctorId: String) {
        db.collection("users")
            .document(doctorId)
            .update("verified", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Doctor verified successfully!", Toast.LENGTH_SHORT).show()
                fetchUnverifiedDoctors()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Verification failed. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Rejects (deletes) a doctor's profile from Firestore.
     *
     * @param doctorId The ID of the doctor to reject.
     */
    private fun rejectDoctor(doctorId: String) {
        db.collection("users")
            .document(doctorId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Doctor rejected and removed.", Toast.LENGTH_SHORT).show()
                fetchUnverifiedDoctors()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to reject doctor.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Displays a dialog to manually add a new doctor.
     * Creates a Firebase account and saves data in Firestore.
     */
    private fun showAddDoctorDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null)
        val nameField = dialogView.findViewById<EditText>(R.id.inputName)
        val surnameField = dialogView.findViewById<EditText>(R.id.inputSurname)
        val emailField = dialogView.findViewById<EditText>(R.id.inputEmail)
        val phoneField = dialogView.findViewById<EditText>(R.id.inputPhone)
        val passwordField = dialogView.findViewById<EditText>(R.id.inputPassword)
        val specField = dialogView.findViewById<EditText>(R.id.inputSpecialization)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Doctor")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameField.text.toString().trim()
                val surname = surnameField.text.toString().trim()
                val email = emailField.text.toString().trim()
                val phone = phoneField.text.toString().trim()
                val password = passwordField.text.toString().trim()
                val spec = specField.text.toString().trim()

                if (name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty()
                    && phone.isNotEmpty() && password.isNotEmpty() && spec.isNotEmpty()
                ) {
                    addDoctor(name, surname, email, phone, password, spec)
                } else {
                    Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.ourgrey)))

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.ourblue)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.nicered)
        )
    }

    /**
     * Creates a new doctor account in Firebase Auth and stores their data in Firestore.
     *
     * @param name Doctor's first name
     * @param surname Doctor's last name
     * @param email Doctor's email
     * @param phone Doctor's phone number
     * @param password Doctor's password
     * @param specialization Doctor's field of specialization
     */
    private fun addDoctor(
        name: String,
        surname: String,
        email: String,
        phone: String,
        password: String,
        specialization: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user!!.uid
                val userData = hashMapOf(
                    "id" to uid,
                    "firstName" to name,
                    "lastName" to surname,
                    "email" to email,
                    "phoneNumber" to phone,
                    "role" to "DOCTOR",
                    "specialization" to specialization,
                    "verified" to true,
                    "profilePictureUrl" to ""
                )

                db.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Doctor added and verified!", Toast.LENGTH_SHORT)
                            .show()
                        fetchUnverifiedDoctors()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save doctor data.", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create doctor account.", Toast.LENGTH_SHORT).show()
            }
    }
}
