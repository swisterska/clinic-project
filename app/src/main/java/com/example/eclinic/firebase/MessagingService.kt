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

class MessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_DEBUG"
        private var notificationId = 0
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "═════════════════════════════════════")
        Log.d(TAG, "🔥 NEW FCM TOKEN RECEIVED: $token")

        val user = FirebaseAuth.getInstance().currentUser?.also {
            Log.d(TAG, "👤 User authenticated: ${it.uid}")
        } ?: run {
            Log.w(TAG, "⚠️ No user logged in - token not saved")
            return
        }

        Log.d(TAG, "🔄 Updating FCM token in Firestore...")
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Token successfully updated")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Token update failed, trying merge", e)
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "🔄 Token saved via merge")
                    }
                    .addOnFailureListener { ex ->
                        Log.e(TAG, "💥 CRITICAL: Failed to save token", ex)
                    }
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "═════════════════════════════════════")
        Log.d(TAG, "📨 NEW MESSAGE RECEIVED")
        Log.d(TAG, "🔹 From: ${remoteMessage.from}")
        Log.d(TAG, "🔹 Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "🔹 Data: ${remoteMessage.data}")
        Log.d(TAG, "🔹 Notification: ${remoteMessage.notification}")

        // Sprawdzanie czy wiadomość zawiera ważne dane
        if (remoteMessage.data.isNullOrEmpty() && remoteMessage.notification == null) {
            Log.e(TAG, "🚫 EMPTY MESSAGE - no data or notification payload")
            return
        }

        val messageBody = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: run {
            Log.w(TAG, "ℹ️ No message body, using default")
            "You have a new notification"
        }
        Log.d(TAG, "📝 Message content: $messageBody")

        // Debugowanie typu wiadomości
        when (remoteMessage.data["type"]) {
            "appointment" -> Log.d(TAG, "📅 Appointment change detected")
            "prescription" -> Log.d(TAG, "💊 Prescription change detected")
            "message" -> Log.d(TAG, "💬 Chat message detected")
            else -> Log.d(TAG, "🔘 Unknown message type")
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "🔄 Processing notification for user ${user.uid}")
            getUserRoleAndUnreadCountAndSendNotification(user.uid, messageBody)
        } else {
            Log.w(TAG, "⚠️ No user - sending basic notification")
            sendNotification(messageBody, "PATIENT", 0)
        }
    }

    private fun getUserRoleAndUnreadCountAndSendNotification(uid: String, messageBody: String) {
        Log.d(TAG, "🔄 Fetching user role for $uid")

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val role = userDoc.getString("role") ?: "PATIENT".also {
                        Log.d(TAG, "👤 User role: $it")
                    }

                    Log.d(TAG, "🔍 Checking unread messages count...")
                    getUnreadMessagesCount(uid) { count ->
                        Log.d(TAG, "📩 Unread messages: $count")
                        sendNotification(messageBody, role, count)
                    }
                } else {
                    Log.w(TAG, "📭 User document doesn't exist")
                    sendNotification(messageBody, "PATIENT", 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to get user role", e)
                sendNotification(messageBody, "PATIENT", 0)
            }
    }

    private fun getUnreadMessagesCount(uid: String, callback: (Int) -> Unit) {
        Log.d(TAG, "🔎 Querying unread messages for $uid")

        FirebaseFirestore.getInstance().collectionGroup("messages")
            .whereEqualTo("toUserId", uid)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size().also {
                    Log.d(TAG, "📊 Found $it unread messages")
                }
                callback(count)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to get unread messages", e)
                callback(0)
            }
    }

    private fun sendNotification(messageBody: String, role: String, unreadMessagesCount: Int) {
        Log.d(TAG, "🛠 Building notification...")
        Log.d(TAG, "🔹 Role: $role")
        Log.d(TAG, "🔹 Unread count: $unreadMessagesCount")

        val targetActivity = if (role.equals("DOCTOR", ignoreCase = true)) {
            Log.d(TAG, "👨‍⚕️ Routing to ChatPatientActivity")
            Intent(this, ChatPatientActivity::class.java)
        } else {
            Log.d(TAG, "👤 Routing to ChatDoctorActivity")
            Intent(this, ChatDoctorActivity::class.java)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            Log.d(TAG, "🔗 Intent flags set")
        }

        val pendingIntent = try {
            PendingIntent.getActivity(
                this,
                notificationId++,
                targetActivity,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ).also {
                Log.d(TAG, "✅ PendingIntent created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ PendingIntent creation failed", e)
            null
        }

        val channelId = "eclinic_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Eclinic")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .apply {
                pendingIntent?.let { setContentIntent(it) }
                if (unreadMessagesCount > 0) {
                    setNumber(unreadMessagesCount)
                    Log.d(TAG, "🔢 Badge count set: $unreadMessagesCount")
                }
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                channelId,
                "Eclinic Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for eClinic notifications"
                enableVibration(true)
                notificationManager.createNotificationChannel(this)
                Log.d(TAG, "📡 Notification channel created")
            }
        }

        val currentNotificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(currentNotificationId, notificationBuilder.build())
        Log.d(TAG, "📢 Notification displayed (ID: $currentNotificationId)")
        Log.d(TAG, "═════════════════════════════════════")
    }
}