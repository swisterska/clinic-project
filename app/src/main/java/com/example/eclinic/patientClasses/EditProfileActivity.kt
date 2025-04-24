package com.example.eclinic.patientClasses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.example.eclinic.logRegClasses.LoginActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var goBackButton: ImageButton
    private lateinit var editName: EditText
    private lateinit var editSurname: EditText
    private lateinit var editBirthDate: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editOldPassword: EditText
    private lateinit var editNewPassword: EditText
    private lateinit var editConfirmNewPassword: EditText
    private lateinit var btnConfirm: androidx.appcompat.widget.AppCompatButton

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        goBackButton = findViewById(R.id.GoBackButton)
        editName = findViewById(R.id.editName)
        editSurname = findViewById(R.id.editSurname)
        editBirthDate = findViewById(R.id.editBirthDate)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        editOldPassword = findViewById(R.id.editOldPassword)
        editNewPassword = findViewById(R.id.editNewPassword)
        editConfirmNewPassword = findViewById(R.id.editConfirmNewPassword)
        btnConfirm = findViewById(R.id.btnConfirm)

        goBackButton.setOnClickListener {
            finish()
        }

        editBirthDate.setOnClickListener {
            showDatePickerDialog()
        }

        loadUserProfile()

        btnConfirm.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun loadUserProfile() {
        val userId = currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    editName.setText(document.getString("firstName") ?: "")
                    editSurname.setText(document.getString("lastName") ?: "")
                    editBirthDate.setText(document.getString("dateOfBirth") ?: "")
                    editEmail.setText(document.getString("email") ?: currentUser?.email ?: "")
                    editPhone.setText(document.getString("phoneNumber") ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile() {
        val userId = currentUser?.uid ?: return
        val userDocument = firestore.collection("users").document(userId)

        val name = editName.text.toString().trim()
        val surname = editSurname.text.toString().trim()
        val birthDate = editBirthDate.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val phone = editPhone.text.toString().trim()

        val updates = hashMapOf<String, Any>()
        if (name.isNotEmpty()) {
            updates["firstName"] = name
        }
        if (surname.isNotEmpty()) {
            updates["lastName"] = surname
        }
        if (birthDate.isNotEmpty()) {
            updates["dateOfBirth"] = birthDate
        }
        if (email.isNotEmpty() && email != currentUser?.email) {
            currentUser?.updateEmail(email)
                ?.addOnSuccessListener {
                    updates["email"] = email
                    updateFirestore(userDocument, updates)
                    Toast.makeText(this, "Email updated. You might need to re-login.", Toast.LENGTH_LONG).show()
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_LONG).show()
                }
            return // Prevent updating Firestore again immediately
        }
        if (phone.isNotEmpty()) {
            updates["phoneNumber"] = phone
        }

        if (updates.isNotEmpty()) {
            updateFirestore(userDocument, updates)
        } else {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        updatePassword()
    }

    private fun updateFirestore(userDocument: com.google.firebase.firestore.DocumentReference, updates: HashMap<String, Any>) {
        userDocument.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                auth.signOut() // Wylogowanie po udanej aktualizacji
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePassword() {
        val oldPassword = editOldPassword.text.toString()
        val newPassword = editNewPassword.text.toString()
        val confirmNewPassword = editConfirmNewPassword.text.toString()

        if (oldPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmNewPassword) {
            currentUser?.reauthenticate(EmailAuthProvider.getCredential(currentUser.email!!, oldPassword))
                ?.addOnSuccessListener {
                    currentUser.updatePassword(newPassword)
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            editOldPassword.text.clear()
                            editNewPassword.text.clear()
                            editConfirmNewPassword.text.clear()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else if (oldPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmNewPassword.isNotEmpty()) {
            Toast.makeText(this, "Please fill all password fields correctly to update password", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editBirthDate.setText(dateFormat.format(selectedDate.time))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}