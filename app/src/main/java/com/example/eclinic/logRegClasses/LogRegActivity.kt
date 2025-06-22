package com.example.eclinic.logRegClasses

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eclinic.R
import com.google.firebase.messaging.FirebaseMessaging

/**
 * The initial activity displayed when the eClinic application is launched.
 * This activity serves as a gateway for users to either log in or register.
 * It also handles requesting necessary permissions (like POST_NOTIFICATIONS for Android 13+)
 * and logs the Firebase Cloud Messaging (FCM) token for debugging purposes.
 */
class LogRegActivity : AppCompatActivity() {

    // Request code for the POST_NOTIFICATIONS permission
    private val REQUEST_CODE_POST_NOTIFICATIONS = 101

    /**
     * Called when the activity is first created.
     * Initializes the layout, sets up click listeners for login and registration,
     * requests POST_NOTIFICATIONS permission (if applicable), and retrieves the FCM token.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_reg_activity)

        // Request POST_NOTIFICATIONS permission for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }

        // Retrieve and log the FCM token for debugging and potential user identification
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "FCM Token: $token")
            } else {
                Log.e("FCM_TOKEN", "Error getting token", task.exception)
            }
        }

        // Set up click listener for the Login TextView
        val textLogin = findViewById<TextView>(R.id.LoginTextView)
        textLogin.isClickable = true
        textLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set up click listener for the Register Button
        val buttonRegister = findViewById<ImageButton>(R.id.ChoiceRegisterButton)
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Callback for the result of requesting permissions.
     * This method is part of the [ActivityCompat.OnRequestPermissionsResultCallback] interface.
     * It specifically handles the result for [REQUEST_CODE_POST_NOTIFICATIONS].
     * @param requestCode The request code passed in [ActivityCompat.requestPermissions].
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED] or [PackageManager.PERMISSION_DENIED].
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Notification permission granted.")
            } else {
                Log.w("Permissions", "Notification permission NOT granted.")
            }
        }
    }
}