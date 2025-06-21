package com.example.eclinic.logRegClasses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.example.eclinic.R
import com.example.eclinic.adminClasses.AdminMainPage
import com.example.eclinic.doctorClasses.MainPageDoctor
import com.example.eclinic.patientClasses.MainPagePatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : BaseActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, LogRegActivity::class.java)
            startActivity(intent)
        }

        inputEmail = findViewById(R.id.emailTextEdit)
        inputPassword = findViewById(R.id.passwordTextEdit)
        loginButton = findViewById(R.id.LoginButton)

        loginButton.setOnClickListener {
            logInRegisteredUser()
        }
    }

    private fun validateLoginDetails(): Boolean {
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        return when {
            email.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> true
        }
    }

    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveFcmToken()        // <-- zapis tokena FCM
                        checkUserRole()
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }

    private fun saveFcmToken() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    val userRef = db.collection("users").document(userId)
                    userRef.set(mapOf("fcmToken" to token), SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("FCM", "Token zapisany do Firestore")
                        }
                        .addOnFailureListener {
                            Log.e("FCM", "Błąd zapisu tokena", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("FCM", "Nie udało się pobrać tokena", it)
                }
        }
    }

    private fun checkUserRole() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "PATIENT"

                        if (role == "DOCTOR") {
                            val verified = document.getBoolean("verified") ?: false
                            if (!verified) {
                                showErrorSnackBar("Your account is pending admin verification.", true)
                                FirebaseAuth.getInstance().signOut()
                                return@addOnSuccessListener
                            }
                            navigateToMainPage(role)
                        } else if (role == "ADMIN") {
                            val intent = Intent(this, AdminMainPage::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            navigateToMainPage(role)
                        }
                    } else {
                        showErrorSnackBar("User data not found.", true)
                    }
                }
                .addOnFailureListener {
                    showErrorSnackBar("Failed to fetch user data.", true)
                }
        }
    }

    private fun navigateToMainPage(role: String) {
        val intent = if (role == "DOCTOR") {
            Intent(this, MainPageDoctor::class.java)
        } else {
            Intent(this, MainPagePatient::class.java)
        }
        startActivity(intent)
        finish()
    }
}
