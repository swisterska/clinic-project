package com.example.eclinic.logRegClasses

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // Stała do obsługi kodu żądania uprawnień
    private val REQUEST_NOTIFICATION_PERMISSION = 100

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

        // Natychmiast po uruchomieniu aktywności prosimy o uprawnienia do powiadomień
        requestNotificationPermission()
    }

    /**
     * Metoda do sprawdzenia i, w razie potrzeby, poproszenia o uprawnienia do wysyłania powiadomień.
     * Jest to wymagane dla Androida 13 (API 33) i nowszych.
     */
    private fun requestNotificationPermission() {
        // Sprawdzamy, czy wersja Androida to 13 (API 33) lub wyższa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Sprawdzamy, czy uprawnienie POST_NOTIFICATIONS zostało już udzielone
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                // Jeśli nie zostało udzielone, prosimy użytkownika o zgodę
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
                Log.d("Permissions", "Requesting POST_NOTIFICATIONS permission.")
            } else {
                Log.d("Permissions", "POST_NOTIFICATIONS permission already granted.")
            }
        } else {
            Log.d("Permissions", "POST_NOTIFICATIONS permission not required for API < 33.")
        }
    }

    /**
     * Callback do obsługi odpowiedzi użytkownika na prośbę o uprawnienia.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uprawnienie zostało udzielone
                Log.d("Permissions", "POST_NOTIFICATIONS permission granted by user.")
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                // Uprawnienie zostało odrzucone
                Log.w("Permissions", "POST_NOTIFICATIONS permission denied by user.")
                Toast.makeText(this, "Notifications may not be displayed without permission. Please enable them in settings.", Toast.LENGTH_LONG).show()
                // Tutaj możesz rozważyć pokazanie dialogu wyjaśniającego, dlaczego potrzebujesz tych uprawnień
            }
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
                        Log.d("Login", "User logged in successfully.")
                        saveFcmToken() // Zapis tokena FCM po udanym logowaniu
                        checkUserRole()
                    } else {
                        Log.e("Login", "Login failed: ${task.exception?.message}")
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
                            Log.d("FCM", "Token zapisany do Firestore.")
                        }
                        .addOnFailureListener {
                            Log.e("FCM", "Błąd zapisu tokena do Firestore.", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("FCM", "Nie udało się pobrać tokena FCM.", it)
                }
        } else {
            Log.w("FCM", "No user ID available to save FCM token.")
        }
    }

    private fun checkUserRole() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "PATIENT"
                        Log.d("Login", "User role: $role")

                        when (role) {
                            "DOCTOR" -> {
                                val verified = document.getBoolean("verified") ?: false
                                if (!verified) {
                                    showErrorSnackBar("Your account is pending admin verification.", true)
                                    FirebaseAuth.getInstance().signOut() // Wyloguj, jeśli niezweryfikowany
                                } else {
                                    navigateToMainPage(role)
                                }
                            }
                            "ADMIN" -> {
                                val intent = Intent(this, AdminMainPage::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else -> { // Domyślnie traktujemy jako PACJENTA, jeśli rola nie jest ani doktorem, ani adminem
                                navigateToMainPage(role)
                            }
                        }
                    } else {
                        showErrorSnackBar("User data not found in Firestore. Please contact support.", true)
                        FirebaseAuth.getInstance().signOut() // Wyloguj, jeśli dane nie istnieją
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Login", "Failed to fetch user data from Firestore: ${e.message}", e)
                    showErrorSnackBar("Failed to fetch user data. Please check your internet connection.", true)
                }
        } ?: run {
            Log.e("Login", "Current user is null after successful login. This should not happen.")
            showErrorSnackBar("An internal error occurred. Please try again.", true)
        }
    }

    private fun navigateToMainPage(role: String) {
        val intent = if (role == "DOCTOR") {
            Intent(this, MainPageDoctor::class.java)
        } else {
            Intent(this, MainPagePatient::class.java)
        }
        startActivity(intent)
        finish() // Zakończ LoginActivity, aby użytkownik nie mógł do niej wrócić przyciskiem wstecz
    }
}