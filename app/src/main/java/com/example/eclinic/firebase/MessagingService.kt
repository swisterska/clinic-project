package com.example.eclinic.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.eclinic.R
import com.example.eclinic.chat.ChatDoctorActivity
import com.example.eclinic.chat.ChatPatientActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service for handling Firebase Cloud Messaging (FCM) messages.
 * This service is responsible for processing new FCM tokens and
 * displaying notifications for incoming messages, particularly chat messages.
 */
class MessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_DEBUG" // Tag for logging messages
    }

    /**
     * Called when a new FCM registration token is generated or updated.
     * This method saves the new token to the currently logged-in user's
     * document in Firebase Firestore.
     * @param token The new FCM registration token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "═════════════════════════════════════")
        Log.d(TAG, "NEW FCM TOKEN RECEIVED: $token")

        val user = FirebaseAuth.getInstance().currentUser?.also {
            Log.d(TAG, "User authenticated: ${it.uid}")
        } ?: run {
            Log.w(TAG, "No user logged in - token not saved")
            return // Exit if no user is logged in
        }

        Log.d(TAG, "Updating FCM token in Firestore...")
        // Attempt to update the token
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "Token successfully updated")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Token update failed, trying merge", e)
                // If update fails (e.g., document doesn't exist or field is missing), try to set/merge
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(mapOf("fcmToken" to token), SetOptions.merge()) // Use SetOptions.merge to add/update the field without overwriting the whole document
                    .addOnSuccessListener {
                        Log.d(TAG, "Token saved via merge")
                    }
                    .addOnFailureListener { ex ->
                        Log.e(TAG, "💥 CRITICAL: Failed to save token", ex) // Log critical error if merge also fails
                    }
            }
    }

    /**
     * Called when an FCM message is received.
     * This method processes the incoming message, extracts its content, and
     * determines the user's role to send a relevant notification.
     * @param remoteMessage The [RemoteMessage] object containing the FCM message payload.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "═════════════════════════════════════")
        Log.d(TAG, "NEW MESSAGE RECEIVED")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Data: ${remoteMessage.data}") // Data payload
        Log.d(TAG, "Notification: ${remoteMessage.notification}") // Notification payload

        // If both notification and data payloads are empty, log and exit
        if (remoteMessage.data.isNullOrEmpty() && remoteMessage.notification == null) {
            Log.e(TAG, "EMPTY MESSAGE - no data or notification payload")
            return
        }

        // Get message body, preferring notification body, then 'message' from data, else default text
        val messageBody = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: run {
            Log.w(TAG, "No message body found, using default text.")
            "You have a new message."
        }
        Log.d(TAG, "Message content: $messageBody")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "Processing notification for user ${user.uid}")
            // Pass remoteMessage.data to the function to access additional information like senderId
            getUserRoleAndSendNotification(user.uid, messageBody, remoteMessage.data)
        } else {
            Log.w(TAG, "No user logged in. Sending basic notification to Patient (default).")
            // If no user is logged in, default to patient role for basic notification
            sendNotification(messageBody, "PATIENT", remoteMessage.data)
        }
    }

    /**
     * Fetches the current user's role from Firestore and then sends a notification.
     * This version is simplified, primarily for chat messages.
     * @param uid The Firebase User ID (UID) of the current user.
     * @param messageBody The main text content of the notification.
     * @param data A map of key-value pairs from the FCM message's data payload.
     */
    private fun getUserRoleAndSendNotification(uid: String, messageBody: String, data: Map<String, String>) {
        Log.d(TAG, "Fetching user role for $uid (simplified for chat messages)")

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val role = userDoc.getString("role") ?: "PATIENT" // Default to "PATIENT" if role is not found
                    Log.d(TAG, "User role: $role")
                    sendNotification(messageBody, role, data)
                } else {
                    Log.w(TAG, "User document doesn't exist. Sending as PATIENT.")
                    sendNotification(messageBody, "PATIENT", data) // Fallback if user document is missing
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get user role. Sending as PATIENT.", e)
                sendNotification(messageBody, "PATIENT", data) // Fallback on failure
            }
    }

    /**
     * Creates and displays a chat notification. It uses data from the FCM message
     * to determine the target activity (ChatDoctorActivity or ChatPatientActivity)
     * and passes necessary information (like chat partner ID) to open the correct chat.
     * @param messageBody The main text content of the notification.
     * @param role The role of the current user ("DOCTOR" or "PATIENT").
     * @param data A map of key-value pairs from the FCM message's data payload,
     * expected to contain 'senderId'.
     */
    private fun sendNotification(messageBody: String, role: String, data: Map<String, String>) {
        Log.d(TAG, "Building chat notification...")
        Log.d(TAG, "Role: $role")
        Log.d(TAG, "Message Data: $data")

        // Check for 'senderId' which is crucial for routing to the correct chat.
        val chatPartnerId = data["senderId"]
        if (chatPartnerId.isNullOrBlank()) {
            Log.e(TAG, "Missing 'senderId' in message data. Cannot route to specific chat.")
            // You might decide to show a generic chat list or no notification at all here.
            // For now, it will proceed but the intent might not open the specific chat.
        }

        val targetIntent: Intent
        if (role.equals("DOCTOR", ignoreCase = true)) {
            Log.d(TAG, "Routing to ChatPatientActivity for Doctor.")
            targetIntent = Intent(this, ChatPatientActivity::class.java)
            if (!chatPartnerId.isNullOrBlank()) {
                targetIntent.putExtra("patientId", chatPartnerId) // For a doctor, chatPartnerId is the patientId
                Log.d(TAG, "Added patientId: $chatPartnerId to intent.")
            }
        } else {
            Log.d(TAG, "Routing to ChatDoctorActivity for Patient.")
            targetIntent = Intent(this, ChatDoctorActivity::class.java)
            if (!chatPartnerId.isNullOrBlank()) {
                targetIntent.putExtra("doctorId", chatPartnerId) // For a patient, chatPartnerId is the doctorId
                Log.d(TAG, "Added doctorId: $chatPartnerId to intent.")
            }
        }

        // Add flags to ensure proper activity stack behavior
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        Log.d(TAG, "Intent flags set for target activity.")

        // Use a unique ID for each notification to allow multiple notifications to be displayed
        val uniqueNotificationId = System.currentTimeMillis().toInt()

        val pendingIntent = try {
            PendingIntent.getActivity(
                this,
                uniqueNotificationId, // Request code for this PendingIntent
                targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE is required for Android 12+
            ).also {
                Log.d(TAG, "PendingIntent created with ID: $uniqueNotificationId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "PendingIntent creation failed: ${e.message}", e)
            null // Return null if PendingIntent creation fails
        }

        val channelId = "eclinic_chat_notifications" // Specific channel ID for chat notifications
        val channelName = "Eclinic Chat Messages" // User-visible name for the channel

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this drawable icon exists in your project
            .setContentTitle("New Message - Eclinic")
            .setContentText(messageBody)
            .setAutoCancel(true) // Notification disappears when tapped
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Default notification sound
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for chat messages
            .apply {
                pendingIntent?.let { setContentIntent(it) } // Set the content intent if successfully created
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel (only needed once per app installation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH // Importance level for the channel
            ).apply {
                description = "Notifications for chat messages in eClinic." // User-visible description
                enableVibration(true) // Enable vibration for this channel
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$channelName' created or updated.")
        }

        // Display the notification
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d(TAG, "Notification displayed (ID: $uniqueNotificationId).")
        Log.d(TAG, "═════════════════════════════════════")
    }
}