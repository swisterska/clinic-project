package com.example.eclinic.logRegClasses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.eclinic.R
import com.example.eclinic.firebase.Specialization
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.util.*

/**
 * Activity responsible for user registration.
 * Allows selecting roles (Patient, Doctor, Admin) and filling in appropriate details.
 * Performs validation and registers the user with Firebase Authentication and Firestore.
 */
class RegisterActivity : BaseActivity() {

    // UI Elements
    private lateinit var btnPatient: Button
    private lateinit var btnDoctor: Button
    private lateinit var btnAdmin: Button
    private lateinit var inputName: EditText
    private lateinit var inputSurname: EditText
    private lateinit var inputDob: EditText
    private lateinit var specializationSpinner: Spinner
    private lateinit var inputEmail: EditText
    private lateinit var inputPhone: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPasswordRepeat: EditText
    private lateinit var registerButton: Button
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Other variables
    private var selectedDob: LocalDate? = null
    private var selectedRole: String = "PATIENT"  // Default role is patient

    /**
     * Initializes UI components, sets up listeners and default states.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, LogRegActivity::class.java)
            startActivity(intent)
        }

        // Initialize views
        btnPatient = findViewById(R.id.btnPatient)
        btnDoctor = findViewById(R.id.btnDoctor)
        inputName = findViewById(R.id.etName)
        inputSurname = findViewById(R.id.etSurname)
        inputDob = findViewById(R.id.etDob)
        specializationSpinner = findViewById(R.id.spinnerSpecialization)
        inputEmail = findViewById(R.id.etEmail)
        inputPhone = findViewById(R.id.etPhone)
        inputPassword = findViewById(R.id.etPassword)
        inputPasswordRepeat = findViewById(R.id.etConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)
        btnAdmin = findViewById(R.id.btnAdmin)

        // Set default button styles
        btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
        btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
        registerButton.setBackgroundColor(ContextCompat.getColor(this, R.color.ourblue))

        // Set role selection listeners
        btnPatient.setOnClickListener {
            selectedRole = "PATIENT"
            toggleButtonSelection()
            inputDob.visibility = EditText.VISIBLE
            specializationSpinner.visibility = EditText.GONE
        }

        btnDoctor.setOnClickListener {
            selectedRole = "DOCTOR"
            toggleButtonSelection()
            inputDob.visibility = EditText.GONE
            specializationSpinner.visibility = EditText.VISIBLE
        }

        btnAdmin.setOnClickListener {
            selectedRole = "ADMIN"
            toggleButtonSelection()
            inputDob.visibility = View.GONE
            specializationSpinner.visibility = View.GONE
        }

        // Register button listener
        registerButton.setOnClickListener {
            registerUser()
        }

        // Show date picker on DOB click
        inputDob.setOnClickListener {
            showDatePickerDialog()
        }

        // Password visibility toggles
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility(inputPassword, btnTogglePassword)
        }

        btnToggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(inputPasswordRepeat, btnToggleConfirmPassword)
        }

        // Setup specialization spinner with options
        val specializationList = mutableListOf("Specialization").apply {
            addAll(Specialization.values().map { it.name })
        }

        val specializationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializationList)
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = specializationAdapter
    }

    /**
     * Shows a DatePicker dialog for selecting date of birth.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dobString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            inputDob.setText(dobString)
        }, year, month, day)

        datePickerDialog.show()
    }

    /**
     * Toggles the visibility of password fields between visible and hidden.
     *
     * @param editText The password EditText to toggle.
     * @param toggleButton The ImageButton which triggers the toggle.
     */
    private fun togglePasswordVisibility(editText: EditText, toggleButton: ImageButton) {
        if (editText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.eyeclosed)
        } else {
            editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.eyeopen)
        }
        editText.setSelection(editText.text.length)
    }

    /**
     * Updates the UI button backgrounds based on currently selected role.
     */
    private fun toggleButtonSelection() {
        when (selectedRole) {
            "PATIENT" -> {
                btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
                btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
                btnAdmin.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
            }
            "DOCTOR" -> {
                btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
                btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
                btnAdmin.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
            }
            "ADMIN" -> {
                btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
                btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
                btnAdmin.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
            }
        }
    }

    /**
     * Validates all input fields according to selected role.
     * Shows error snackbars for invalid or missing inputs.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            inputName.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_name), true)
                false
            }
            inputSurname.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_surname), true)
                false
            }
            inputEmail.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }
            inputPhone.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_phone), true)
                false
            }
            inputPassword.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }
            inputPasswordRepeat.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_confpassword), true)
                false
            }
            inputPassword.text.toString() != inputPasswordRepeat.text.toString() -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }
            selectedRole == "PATIENT" && inputDob.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_dob), true)
                false
            }
            selectedRole == "DOCTOR" && (specializationSpinner.selectedItem.toString() == "Specialization" || specializationSpinner.selectedItem.toString().trim().isEmpty()) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_specialization), true)
                false
            }
            else -> true
        }
    }

    /**
     * Registers a new user with Firebase Authentication and saves user details to Firestore.
     * Includes role-based additional data (DOB, specialization, verification status).
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val name = inputName.text.toString().trim()
            val surname = inputSurname.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val phone = inputPhone.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val dob = inputDob.text.toString().trim()
            val selectedSpec = specializationSpinner.selectedItem.toString().trim()
            val specialization = Specialization.fromString(selectedSpec)?.displayName ?: selectedSpec
            val role = selectedRole

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        val userData = hashMapOf<String, Any>(
                            "id" to firebaseUser.uid,
                            "firstName" to name,
                            "lastName" to surname,
                            "email" to email,
                            "phoneNumber" to phone,
                            "role" to role,
                            "profilePictureUrl" to ""
                        )

                        when (role) {
                            "PATIENT" -> {
                                userData["dateOfBirth"] = dob
                            }
                            "DOCTOR" -> {
                                userData["specialization"] = specialization
                                userData["verified"] = false
                            }
                            "ADMIN" -> {
                                userData["verified"] = true
                            }
                        }

                        FirebaseFirestore.getInstance().collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@RegisterActivity, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Navigates to the login screen.
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
