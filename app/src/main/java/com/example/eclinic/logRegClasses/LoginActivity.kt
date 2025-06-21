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

/**
 * [LoginActivity] handles user login functionality.
 * It allows users to log in with their email and password,
 * checks their role (Patient, Doctor, Admin), and redirects them
 * to the appropriate main page. It also handles requesting notification permissions
 * for Android 13+ and saving the FCM token to Firestore upon successful login.
 */
class LoginActivity : BaseActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginButton: Button
    private val db = FirebaseFirestore.getInstance()

    // Constant for handling permission request code
    private val REQUEST_NOTIFICATION_PERMISSION = 100

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up click listeners, and requests notification permissions.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
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

        // Request notification permission immediately upon activity launch
        requestNotificationPermission()
    }

    /**
     * Checks for and, if necessary, requests permission to send notifications.
     * This is required for Android 13 (API 33) and newer.
     */
    private fun requestNotificationPermission() {
        // Check if the Android version is 13 (API 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the POST_NOTIFICATIONS permission has already been granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                // If not granted, request the user's consent
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
     * Callback for handling the user's response to a permission request.
     * @param requestCode The request code passed in [ActivityCompat.requestPermissions].
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("Permissions", "POST_NOTIFICATIONS permission granted by user.")
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Log.w("Permissions", "POST_NOTIFICATIONS permission denied by user.")
                Toast.makeText(this, "Notifications may not be displayed without permission. Please enable them in settings.", Toast.LENGTH_LONG).show()
                // Consider showing an explanatory dialog here about why the permission is needed
            }
        }
    }

    /**
     * Validates the login details (email and password) entered by the user.
     * Displays a [Toast] or [showErrorSnackBar] if any field is empty.
     * @return `true` if the details are valid, `false` otherwise.
     */
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

    /**
     * Attempts to log in the registered user using Firebase Authentication.
     * If successful, it saves the FCM token and then checks the user's role
     * to navigate to the appropriate main page.
     * Displays error messages for failed login attempts.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Login", "User logged in successfully.")
                        saveFcmToken() // Save FCM token upon successful login
                        checkUserRole()
                    } else {
                        Log.e("Login", "Login failed: ${task.exception?.message}")
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Retrieves the Firebase Cloud Messaging (FCM) token for the current user's device
     * and saves it to their user document in Firestore. This is crucial for sending
     * push notifications to the user.
     */
    private fun saveFcmToken() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    val userRef = db.collection("users").document(userId)
                    userRef.set(mapOf("fcmToken" to token), SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("FCM", "Token saved to Firestore successfully.")
                        }
                        .addOnFailureListener {
                            Log.e("FCM", "Error saving token to Firestore.", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("FCM", "Failed to retrieve FCM token.", it)
                }
        } else {
            Log.w("FCM", "No user ID available to save FCM token.")
        }
    }

    /**
     * Checks the role of the currently logged-in user from Firestore
     * and navigates them to the appropriate main activity based on their role.
     * Handles specific verification logic for DOCTOR roles.
     */
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
                                    FirebaseAuth.getInstance().signOut() // Log out if not verified
                                } else {
                                    navigateToMainPage(role)
                                }
                            }
                            "ADMIN" -> {
                                val intent = Intent(this, AdminMainPage::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else -> { // Default to PATIENT if role is neither DOCTOR nor ADMIN
                                navigateToMainPage(role)
                            }
                        }
                    } else {
                        showErrorSnackBar("User data not found in Firestore. Please contact support.", true)
                        FirebaseAuth.getInstance().signOut() // Log out if user data doesn't exist
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

    /**
     * Navigates the user to their respective main activity based on their role.
     * Finishes the current [LoginActivity] to prevent returning to it via the back button.
     * @param role The role of the user ("DOCTOR" or "PATIENT").
     */
    private fun navigateToMainPage(role: String) {
        val intent = if (role == "DOCTOR") {
            Intent(this, MainPageDoctor::class.java)
        } else {
            Intent(this, MainPagePatient::class.java)
        }
        startActivity(intent)
        finish() // Finish LoginActivity so user cannot return to it with back button
    }
}