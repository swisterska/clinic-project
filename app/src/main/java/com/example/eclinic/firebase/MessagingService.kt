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
 * Firebase Messaging Service to handle FCM token updates and incoming messages.
 */
class MessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_DEBUG"
    }

    /**
     * Called when a new FCM token is generated.
     * Updates the token in the user's Firestore document.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "═════════════════════════════════════")
        Log.d(TAG, "NEW FCM TOKEN RECEIVED: $token")

        val user = FirebaseAuth.getInstance().currentUser?.also {
            Log.d(TAG, "User authenticated: ${it.uid}")
        } ?: run {
            Log.w(TAG, "No user logged in - token not saved")
            return
        }

        Log.d(TAG, "Updating FCM token in Firestore...")
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "Token successfully updated")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Token update failed, trying merge", e)
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Token saved via merge")
                    }
                    .addOnFailureListener { ex ->
                        Log.e(TAG, "CRITICAL: Failed to save token", ex)
                    }
            }
    }

    /**
     * Called when a new FCM message is received.
     * Processes message payload and displays appropriate notification.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "═════════════════════════════════════")
        Log.d(TAG, "NEW MESSAGE RECEIVED")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")

        // If no notification or data payload, log and return
        if (remoteMessage.data.isNullOrEmpty() && remoteMessage.notification == null) {
            Log.e(TAG, "EMPTY MESSAGE - no data or notification payload")
            return
        }

        // Get message content from notification or data payload
        val messageBody = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: run {
            Log.w(TAG, "No message body found, using default text.")
            "You have a new message."
        }
        Log.d(TAG, "Message content: $messageBody")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "Processing notification for user ${user.uid}")
            getUserRoleAndSendNotification(user.uid, messageBody, remoteMessage.data)
        } else {
            Log.w(TAG, "No user logged in. Sending basic notification to Patient.")
            sendNotification(messageBody, "PATIENT", remoteMessage.data)
        }
    }

    /**
     * Retrieves the user's role from Firestore and routes the notification accordingly.
     * Only used for chat messages.
     *
     * @param uid The user ID.
     * @param messageBody The content of the message.
     * @param data Additional data from the FCM message.
     */
    private fun getUserRoleAndSendNotification(uid: String, messageBody: String, data: Map<String, String>) {
        Log.d(TAG, "Fetching user role for $uid (simplified for chat messages)")

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val role = userDoc.getString("role") ?: "PATIENT" // Default to Patient
                    Log.d(TAG, "User role: $role")
                    sendNotification(messageBody, role, data)
                } else {
                    Log.w(TAG, "User document doesn't exist. Sending as PATIENT.")
                    sendNotification(messageBody, "PATIENT", data)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get user role. Sending as PATIENT.", e)
                sendNotification(messageBody, "PATIENT", data)
            }
    }

    /**
     * Builds and shows a chat notification based on the user's role and message data.
     * Includes logic to route to the appropriate chat activity (Doctor or Patient).
     *
     * @param messageBody The content of the message to display.
     * @param role The user's role (DOCTOR or PATIENT).
     * @param data Additional key-value data from the FCM payload.
     */
    private fun sendNotification(messageBody: String, role: String, data: Map<String, String>) {
        Log.d(TAG, "Building chat notification...")
        Log.d(TAG, "Role: $role")
        Log.d(TAG, "Message Data: $data")

        // Check for senderId; used to route to specific chat
        val chatPartnerId = data["senderId"]
        if (chatPartnerId.isNullOrBlank()) {
            Log.e(TAG, "Missing 'senderId' in message data. Cannot route to specific chat.")
            // Consider whether to show a general chat screen or nothing
        }

        val targetIntent: Intent
        if (role.equals("DOCTOR", ignoreCase = true)) {
            Log.d(TAG, "Routing to ChatPatientActivity for Doctor.")
            targetIntent = Intent(this, ChatPatientActivity::class.java)
            if (!chatPartnerId.isNullOrBlank()) {
                targetIntent.putExtra("patientId", chatPartnerId) // For doctor, chatPartnerId is patientId
                Log.d(TAG, "🔗 Added patientId: $chatPartnerId to intent.")
            }
        } else {
            Log.d(TAG, "Routing to ChatDoctorActivity for Patient.")
            targetIntent = Intent(this, ChatDoctorActivity::class.java)
            if (!chatPartnerId.isNullOrBlank()) {
                targetIntent.putExtra("doctorId", chatPartnerId) // For patient, chatPartnerId is doctorId
                Log.d(TAG, "🔗 Added doctorId: $chatPartnerId to intent.")
            }
        }

        // Add flags to Intent
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        Log.d(TAG, "Intent flags set for target activity.")

        // Generate a unique notification ID
        val uniqueNotificationId = System.currentTimeMillis().toInt()

        val pendingIntent = try {
            PendingIntent.getActivity(
                this,
                uniqueNotificationId,
                targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ).also {
                Log.d(TAG, "PendingIntent created with ID: $uniqueNotificationId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "PendingIntent creation failed: ${e.message}", e)
            null
        }

        val channelId = "eclinic_chat_notifications"
        val channelName = "Eclinic Chat Messages"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists in resources
            .setContentTitle("New Message - Eclinic")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                pendingIntent?.let { setContentIntent(it) }
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel if required (Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat messages in eClinic."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$channelName' created or updated.")
        }

        // Show the notification
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d(TAG, "Notification displayed (ID: $uniqueNotificationId).")
        Log.d(TAG, "═════════════════════════════════════")
    }
}
