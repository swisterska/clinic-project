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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New device token: $token")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "FCM token updated in user document")
                }
                .addOnFailureListener { e ->
                    userDocRef.set(mapOf("fcmToken" to token), SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("FCM", "FCM token saved in new user document")
                        }
                        .addOnFailureListener { ex ->
                            Log.e("FCM", "Error saving FCM token", ex)
                        }
                }
        } else {
            Log.w("FCM", "No logged-in user, token not saved")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "FCM message received")

        val messageBody = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "You have a new notification"

        Log.d("FCM", "Notification content: $messageBody")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            getUserRoleAndUnreadCountAndSendNotification(user.uid, messageBody)
        } else {
            sendNotification(messageBody, "PATIENT", 0)
        }
    }

    private fun getUserRoleAndUnreadCountAndSendNotification(uid: String, messageBody: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val role = userDoc.getString("role") ?: "PATIENT"
                    getUnreadMessagesCount(uid) { count ->
                        sendNotification(messageBody, role, count)
                    }
                } else {
                    sendNotification(messageBody, "PATIENT", 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error getting user role", e)
                sendNotification(messageBody, "PATIENT", 0)
            }
    }

    private fun getUnreadMessagesCount(uid: String, callback: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collectionGroup("messages")
            .whereEqualTo("toUserId", uid)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("FCM", "Unread messages: ${querySnapshot.size()}")
                callback(querySnapshot.size())
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error getting messages", e)
                callback(0)
            }
    }

    private fun sendNotification(messageBody: String, role: String, unreadMessagesCount: Int) {
        val intent = if (role.uppercase() == "DOCTOR") {
            Intent(this, ChatPatientActivity::class.java)
        } else {
            Intent(this, ChatDoctorActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "eclinic_notifications"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Eclinic")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setNumber(unreadMessagesCount)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Eclinic Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}