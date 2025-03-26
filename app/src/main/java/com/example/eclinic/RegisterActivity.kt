package com.example.eclinic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.eclinic.firebase.Firestore
import com.example.eclinic.firebase.User
import kotlinx.coroutines.launch




class RegisterActivity : BaseActivity() {

    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioPatient: RadioButton
    private lateinit var radioDoctor: RadioButton
    private lateinit var inputName: EditText
    private lateinit var inputSurname: EditText
    private lateinit var inputDob: EditText
    private lateinit var inputSpecialization: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPhone: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPasswordRepeat: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        // Initialize all views
        radioGroupType = findViewById(R.id.radioGroupType)
        radioPatient = findViewById(R.id.radioPatient)
        radioDoctor = findViewById(R.id.radioDoctor)
        inputName = findViewById(R.id.etName)
        inputSurname = findViewById(R.id.etSurname)
        inputDob = findViewById(R.id.etDob)
        inputSpecialization = findViewById(R.id.etSpecialization)
        inputEmail = findViewById(R.id.etEmail)
        inputPhone = findViewById(R.id.etPhone)
        inputPassword = findViewById(R.id.etPassword)
        inputPasswordRepeat = findViewById(R.id.etConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)

        // Show/hide fields dynamically
        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioPatient) {
                inputDob.visibility = EditText.VISIBLE
                inputSpecialization.visibility = EditText.GONE
            } else {
                inputDob.visibility = EditText.GONE
                inputSpecialization.visibility = EditText.VISIBLE
            }
        }

        registerButton.setOnClickListener {
            registerUser()
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
            radioPatient.isChecked && inputDob.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_dob), true)
                false
            }
            radioDoctor.isChecked && inputSpecialization.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_specialization), true)
                false
            }
            else -> true
        }
    }

    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
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
            val type = if (radioPatient.isChecked) "patient" else "doctor"

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )


                        val user = User(
                            id = firebaseUser.uid,
                            firstName = name,
                            lastName = surname,
                            email = email,
                            phoneNumber = phone,
                            profilePictureUrl = ""

                        )
                        if (type == "patient") {
                            user.dateOfBirth = dob
                        } else {
                            user.specialization = specialization
                        }

                        // Save data to Firestore
                        lifecycleScope.launch {
                            try {
                                val firestore = Firestore()
                                firestore.registerOrUpdateUser(user)
                                Toast.makeText(this@RegisterActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()

                            } catch (e: Exception) {
                                Toast.makeText(this@RegisterActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        FirebaseAuth.getInstance().signOut()
                        finish()

                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    fun userRegistrationSuccess() {
        Toast.makeText(
            this@RegisterActivity,
            getString(R.string.register_success),
            Toast.LENGTH_SHORT
        ).show()
    }
}
