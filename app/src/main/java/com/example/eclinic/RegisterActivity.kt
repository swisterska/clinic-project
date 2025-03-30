package com.example.eclinic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.util.Calendar
import android.app.DatePickerDialog

class RegisterActivity : BaseActivity() {

    // Declare variables for UI elements
    private lateinit var btnPatient: Button
    private lateinit var btnDoctor: Button
    private lateinit var inputName: EditText
    private lateinit var inputSurname: EditText
    private lateinit var inputDob: EditText
    private lateinit var inputSpecialization: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPhone: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPasswordRepeat: EditText
    private lateinit var registerButton: Button

    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedDob: LocalDate? = null




    private var selectedRole: String = "PATIENT"  // Default to Patient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, LogRegActivity::class.java)
            startActivity(intent)
        }

        // Initialize all views
        btnPatient = findViewById(R.id.btnPatient)
        btnDoctor = findViewById(R.id.btnDoctor)
        inputName = findViewById(R.id.etName)
        inputSurname = findViewById(R.id.etSurname)
        inputDob = findViewById(R.id.etDob)
        inputSpecialization = findViewById(R.id.etSpecialization)
        inputEmail = findViewById(R.id.etEmail)
        inputPhone = findViewById(R.id.etPhone)
        inputPassword = findViewById(R.id.etPassword)
        inputPasswordRepeat = findViewById(R.id.etConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)

        // Set default selection to Patient
        btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
        btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
        registerButton.setBackgroundColor(ContextCompat.getColor(this, R.color.ourblue))

        // Handle toggle between Patient and Doctor
        btnPatient.setOnClickListener {
            selectedRole = "PATIENT"
            toggleButtonSelection()
            inputDob.visibility = EditText.VISIBLE
            inputSpecialization.visibility = EditText.GONE
        }

        btnDoctor.setOnClickListener {
            selectedRole = "DOCTOR"
            toggleButtonSelection()
            inputDob.visibility = EditText.GONE
            inputSpecialization.visibility = EditText.VISIBLE
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

    }


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



    // Toggle button selection (highlight the selected role button)
    private fun toggleButtonSelection() {
        if (selectedRole == "PATIENT") {
            btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
            btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
        } else {
            btnPatient.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedbutton))
            btnDoctor.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedbutton))
        }
    }

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
            selectedRole == "DOCTOR" && inputSpecialization.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_specialization), true)
                false
            }
            else -> true
        }
    }

    private fun registerUser() {
        if (validateRegisterDetails()) {
            val name = inputName.text.toString().trim()
            val surname = inputSurname.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val phone = inputPhone.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val dob = inputDob.text.toString().trim()
            val specialization = inputSpecialization.text.toString().trim()
            val role = selectedRole  // Use the selected role (either "PATIENT" or "DOCTOR")

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        // Create a HashMap to store user details
                        val userData = hashMapOf(
                            "id" to firebaseUser.uid,
                            "firstName" to name,
                            "lastName" to surname,
                            "email" to email,
                            "phoneNumber" to phone,
                            "role" to role,
                            "profilePictureUrl" to "",
                        )

                        // Add additional fields based on role
                        if (role == "PATIENT") {
                            userData["dateOfBirth"] = dob
                        } else {
                            userData["specialization"] = specialization
                        }



                        // Save user data to Firestore
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

    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
