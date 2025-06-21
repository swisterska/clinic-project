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
 * [LogRegActivity] serves as the initial entry point for users,
 * allowing them to choose between logging in or registering a new account.
 * It also handles requesting notification permissions for Android 13 (API 33) and above,
 * and logs the Firebase Cloud Messaging (FCM) token for debugging purposes.
 */
class LogRegActivity : AppCompatActivity() {

    private val REQUEST_CODE_POST_NOTIFICATIONS = 101

    /**
     * Called when the activity is first created.
     * Initializes UI elements and sets up click listeners for navigation to
     * [LoginActivity] and [RegisterActivity].
     * Also requests POST_NOTIFICATIONS permission for Android 13+ and logs FCM token.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_reg_activity)

        // Request POST_NOTIFICATIONS permission for Android 13 (API 33) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }

        // Retrieve and log the Firebase Cloud Messaging (FCM) token.
        // This token is essential for sending push notifications to the device.
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "FCM Token: $token")
            } else {
                Log.e("FCM_TOKEN", "Error getting token", task.exception)
            }
        }

        // Set up click listener for the Login TextView to navigate to LoginActivity.
        val textLogin = findViewById<TextView>(R.id.LoginTextView)
        textLogin.isClickable = true
        textLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set up click listener for the Register Button to navigate to RegisterActivity.
        val buttonRegister = findViewById<ImageButton>(R.id.ChoiceRegisterButton)
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Callback for the result of requesting permissions.
     * This method is called after [ActivityCompat.requestPermissions] is invoked.
     * It specifically handles the result for the [REQUEST_CODE_POST_NOTIFICATIONS] permission.
     * @param requestCode The integer request code originally supplied to [ActivityCompat.requestPermissions].
     * @param permissions An array of requested permissions.
     * @param grantResults An array of granted or denied permissions.
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