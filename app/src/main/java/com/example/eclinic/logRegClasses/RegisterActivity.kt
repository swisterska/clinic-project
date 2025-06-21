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
 * [RegisterActivity] handles the user registration process for different roles (Patient, Doctor, Admin).
 * It collects user input, validates it, creates a new user account with Firebase Authentication,
 * and saves additional user details to Firebase Firestore.
 *
 * It provides UI for:
 * - Selecting a user role.
 * - Inputting personal details (name, surname, email, phone).
 * - Entering date of birth for patients.
 * - Selecting specialization for doctors.
 * - Setting and confirming a password.
 * - Toggling password visibility.
 */
class RegisterActivity : BaseActivity() {

    // UI Elements
    private lateinit var btnPatient: Button
    private lateinit var btnDoctor: Button
    private lateinit var btnAdmin:Button
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

    // Other
    private var selectedDob: LocalDate? = null
    private var selectedRole: String = "PATIENT"  // Default role

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up Firebase instances, and configures event listeners.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

        // Apply default button styles and initial visibility for input fields
        btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
        btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
        btnAdmin.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton)) // Ensure admin is also unselected by default
        registerButton.setBackgroundColor(ContextCompat.getColor(this, R.color.ourblue))
        specializationSpinner.visibility = View.GONE // Hide specialization spinner initially

        // Role toggle buttons setup
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
            inputDob.visibility = View.GONE // Admins don't need DOB
            specializationSpinner.visibility = View.GONE // Admins don't need specialization
        }

        registerButton.setOnClickListener {
            registerUser()
        }

        inputDob.setOnClickListener {
            showDatePickerDialog()
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility(inputPassword, btnTogglePassword)
        }

        btnToggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(inputPasswordRepeat, btnToggleConfirmPassword)
        }

        // Spinner setup for Specialization
        val specializationList = mutableListOf("Specialization").apply {
            // Add all specialization display names from the enum
            addAll(Specialization.values().map { it.displayName }) // Use displayName
        }

        val specializationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializationList)
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = specializationAdapter
    }

    /**
     * Displays a [DatePickerDialog] to allow the user to select their date of birth.
     * The selected date is then formatted and set to the [inputDob] EditText.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date to DD/MM/YYYY
            val dobString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            inputDob.setText(dobString)
        }, year, month, day)

        // Optional: Set max date to today to prevent future dates
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis

        datePickerDialog.show()
    }

    /**
     * Toggles the visibility of the password in an [EditText] field.
     * Changes the input type and updates the icon of the associated [ImageButton].
     * @param editText The [EditText] whose password visibility is to be toggled.
     * @param toggleButton The [ImageButton] that triggers the toggle and whose icon will be updated.
     */
    private fun togglePasswordVisibility(editText: EditText, toggleButton: ImageButton) {
        if (editText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // If currently visible, hide password
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.eyeclosed)
        } else {
            // If currently hidden, show password
            editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.eyeopen)
        }
        // Keep the cursor at the end of the text after toggling
        editText.setSelection(editText.text.length)
    }

    /**
     * Updates the background color of the role selection buttons based on the [selectedRole].
     * The selected role's button will have a different background color to indicate selection.
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
     * Validates all input fields required for registration based on the [selectedRole].
     * Displays an error message using [showErrorSnackBar] if any validation fails.
     * @return `true` if all details are valid, `false` otherwise.
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
            // Specific validation for Patient role
            selectedRole == "PATIENT" && inputDob.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_dob), true)
                false
            }
            // Specific validation for Doctor role
            selectedRole == "DOCTOR" && (specializationSpinner.selectedItem.toString() == "Specialization" || specializationSpinner.selectedItem.toString().trim().isEmpty()) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_specialization), true)
                false
            }
            else -> true
        }
    }

    /**
     * Initiates the user registration process.
     * If validation passes, it attempts to create a user with Firebase Authentication.
     * On success, it saves additional user data (including role-specific fields) to Firestore.
     * Navigates to [LoginActivity] upon successful registration.
     * Displays error messages for registration failures.
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
            // Get the display name of the specialization from the enum
            val specialization = Specialization.fromString(selectedSpec)?.displayName ?: selectedSpec
            val role = selectedRole

            // Create user with Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        // Prepare user data for Firestore
                        val userData = hashMapOf<String, Any>(
                            "id" to firebaseUser.uid,
                            "firstName" to name,
                            "lastName" to surname,
                            "email" to email,
                            "phoneNumber" to phone,
                            "role" to role,
                            "profilePictureUrl" to "" // Initialize with empty string
                        )

                        // Add role-specific data
                        when (role) {
                            "PATIENT" -> {
                                userData["dateOfBirth"] = dob
                            }
                            "DOCTOR" -> {
                                userData["specialization"] = specialization
                                userData["verified"] = false // Doctors need to be verified by admin
                            }
                            "ADMIN" -> {
                                userData["verified"] = true // Admins are verified by default
                            }
                        }

                        // Save user data to Firestore
                        FirebaseFirestore.getInstance().collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish() // Finish current activity
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@RegisterActivity, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Registration failed (e.g., email already in use, weak password)
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Navigates to the [LoginActivity]. This method is typically called when
     * the user decides to go back to the login screen without completing registration.
     * @param view The [View] that triggered this method (e.g., a button).
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}