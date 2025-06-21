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

        // Jeśli brakuje powiadomienia lub danych, logujemy i kończymy
        if (remoteMessage.data.isNullOrEmpty() && remoteMessage.notification == null) {
            Log.e(TAG, "🚫 EMPTY MESSAGE - no data or notification payload")
            return
        }

        // Pobierz treść wiadomości z notification payload lub data payload
        val messageBody = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: run {
            Log.w(TAG, "ℹ️ No message body found, using default text.")
            "You have a new message."
        }
        Log.d(TAG, "📝 Message content: $messageBody")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "🔄 Processing notification for user ${user.uid}")
            // Przekazujemy remoteMessage.data do funkcji, aby mieć dostęp do dodatkowych informacji
            getUserRoleAndSendNotification(user.uid, messageBody, remoteMessage.data)
        } else {
            Log.w(TAG, "⚠️ No user logged in. Sending basic notification to Patient.")
            sendNotification(messageBody, "PATIENT", remoteMessage.data)
        }
    }

    /**
     * Pobiera rolę użytkownika i wysyła powiadomienie.
     * Uproszczona wersja tylko dla wiadomości czatu.
     */
    private fun getUserRoleAndSendNotification(uid: String, messageBody: String, data: Map<String, String>) {
        Log.d(TAG, "🔄 Fetching user role for $uid (simplified for chat messages)")

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val role = userDoc.getString("role") ?: "PATIENT" // Domyślnie Patient
                    Log.d(TAG, "👤 User role: $role")
                    sendNotification(messageBody, role, data)
                } else {
                    Log.w(TAG, "📭 User document doesn't exist. Sending as PATIENT.")
                    sendNotification(messageBody, "PATIENT", data)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to get user role. Sending as PATIENT.", e)
                sendNotification(messageBody, "PATIENT", data)
            }
    }

    /**
     * Tworzy i wyświetla powiadomienie czatu.
     * Przekazuje dane do Intentu, aby otworzyć odpowiedni czat.
     */
    private fun sendNotification(messageBody: String, role: String, data: Map<String, String>) {
        Log.d(TAG, "🛠 Building chat notification...")
        Log.d(TAG, "🔹 Role: $role")
        Log.d(TAG, "🔹 Message Data: $data")

        // Sprawdź, czy senderId istnieje w danych. Jest to kluczowe dla otwarcia odpowiedniego czatu.
        val chatPartnerId = data["senderId"]
        if (chatPartnerId.isNullOrBlank()) {
            Log.e(TAG, "🚫 Missing 'senderId' in message data. Cannot route to specific chat.")
            // Możesz tutaj zdecydować, czy mimo to wyświetlić powiadomienie i otworzyć ogólną stronę czatów,
            // czy w ogóle nie wyświetlać powiadomienia. Na razie wyświetlimy bez konkretnego routingu.
        }

        val targetIntent: Intent
        if (role.equals("DOCTOR", ignoreCase = true)) {
            Log.d(TAG, "👨‍⚕️ Routing to ChatPatientActivity for Doctor.")
            targetIntent = Intent(this, ChatPatientActivity::class.java)
            if (!chatPartnerId.isNullOrBlank()) {
                targetIntent.putExtra("patientId", chatPartnerId) // Dla lekarza, chatPartnerId to patientId
                Log.d(TAG, "🔗 Added patientId: $chatPartnerId to intent.")
            }
        } else {
            Log.d(TAG, "👤 Routing to ChatDoctorActivity for Patient.")
            targetIntent = Intent(this, ChatDoctorActivity::class.java)
            if (!chatPartnerId.isNullOrBlank()) {
                targetIntent.putExtra("doctorId", chatPartnerId) // Dla pacjenta, chatPartnerId to doctorId
                Log.d(TAG, "🔗 Added doctorId: $chatPartnerId to intent.")
            }
        }

        // Dodaj flagi dla Intentu
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        Log.d(TAG, "🔗 Intent flags set for target activity.")

        // Użyj unikalnego ID powiadomienia
        val uniqueNotificationId = System.currentTimeMillis().toInt()

        val pendingIntent = try {
            PendingIntent.getActivity(
                this,
                uniqueNotificationId,
                targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ).also {
                Log.d(TAG, "✅ PendingIntent created with ID: $uniqueNotificationId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ PendingIntent creation failed: ${e.message}", e)
            null
        }

        val channelId = "eclinic_chat_notifications" // Zmieniono ID kanału dla jasności
        val channelName = "Eclinic Chat Messages"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Upewnij się, że ta ikona istnieje
            .setContentTitle("New Message - Eclinic")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                pendingIntent?.let { setContentIntent(it) }
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tworzenie kanału powiadomień (tylko raz na instalację aplikacji)
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
            Log.d(TAG, "📡 Notification channel '$channelName' created or updated.")
        }

        // Wyświetl powiadomienie
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d(TAG, "📢 Notification displayed (ID: $uniqueNotificationId).")
        Log.d(TAG, "═════════════════════════════════════")
    }
}