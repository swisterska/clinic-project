package com.example.eclinic.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.FirebaseAuth
import com.example.eclinic.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

class ChatDoctorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var inputEditText: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var patientNameTextView: TextView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var currentUserId: String = ""
    private var patientId: String = ""
    private var patientName: String = ""
    private var chatId: String? = null

    private var chatListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_doctor)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserId = auth.currentUser?.uid ?: run {
            Log.e("ChatDoctorActivity", "Current user ID is null. User not authenticated. Finishing activity.")
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("ChatDoctorActivity", "Current User ID (logged in): $currentUserId")

        patientId = intent.getStringExtra("patientId") ?: run {
            Log.e("ChatDoctorActivity", "patientId not provided in Intent. Finishing activity.")
            Toast.makeText(this, "Error: Other chat participant ID missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        patientName = intent.getStringExtra("patientName") ?: "Unknown User"
        Log.d("ChatDoctorActivity", "Other participant ID (patientId): $patientId")
        Log.d("ChatDoctorActivity", "Other participant Name: $patientName")

        supportActionBar?.title = patientName

        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)

        adapter = ChatAdapter(messages, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            val userText = inputEditText.text.toString().trim()
            if (userText.isNotBlank()) {
                sendMessage(userText)
                inputEditText.text?.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        findOrCreateChat()
    }

    private fun findOrCreateChat() {
        val participantsSorted = listOf(currentUserId, patientId).sorted()
        val generatedChatId = "${participantsSorted[0]}_${participantsSorted[1]}"
        Log.d("ChatDoctorActivity", "Generated chat ID (sorted): $generatedChatId")

        firestore.collection("chats").document(generatedChatId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    chatId = generatedChatId
                    Log.d("ChatDoctorActivity", "Found existing chat with ID: $chatId")
                    listenForMessages()
                } else {
                    val newChatData = hashMapOf(
                        "participants" to participantsSorted,
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "lastMessageText" to "New conversation started."
                    )
                    firestore.collection("chats").document(generatedChatId)
                        .set(newChatData)
                        .addOnSuccessListener {
                            chatId = generatedChatId
                            Log.d("ChatDoctorActivity", "Created new chat with ID: $chatId")
                            listenForMessages()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatDoctorActivity", "Error creating chat with ID: $generatedChatId", e)
                            Toast.makeText(this, "Error creating chat: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatDoctorActivity", "Error finding or creating chat document: ${e.message}", e)
                Toast.makeText(this, "Error finding chat: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun sendMessage(text: String) {
        if (chatId == null) {
            Log.e("ChatDoctorActivity", "Chat ID is null, cannot send message. Re-finding chat...")
            Toast.makeText(this, "Chat not ready yet, please try again.", Toast.LENGTH_SHORT).show()
            findOrCreateChat()
            return
        }

        val chatMessage = ChatMessage(
            senderId = currentUserId,
            receiverId = patientId,
            messageText = text
        )

        firestore.collection("chats").document(chatId!!)
            .collection("messages")
            .add(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatDoctorActivity", "Message sent successfully to chat $chatId.")
                firestore.collection("chats").document(chatId!!)
                    .update(
                        "lastMessageTimestamp", FieldValue.serverTimestamp(),
                        "lastMessageText", text
                    )
                    .addOnSuccessListener {
                        Log.d("ChatDoctorActivity", "Chat metadata (timestamp, text) updated.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatDoctorActivity", "Error updating chat metadata: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatDoctorActivity", "Error sending message: ${e.message}", e)
                Toast.makeText(this, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        if (chatId == null) {
            Log.e("ChatDoctorActivity", "Chat ID is null, cannot listen for messages. Waiting for chat to be found/created.")
            return
        }

        chatListener?.remove()

        Log.d("ChatDoctorActivity", "Starting to listen for messages in chat: $chatId")

        chatListener = firestore.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatDoctorActivity", "Listen for messages failed.", e)
                    Toast.makeText(this, "Error loading messages: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    Log.d("ChatDoctorActivity", "Received ${snapshots.documents.size} messages for chat $chatId.")
                    messages.clear()
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(ChatMessage::class.java)
                        message?.let {
                            messages.add(it)
                            Log.d("ChatDoctorActivity", "Message loaded: Sender: ${it.senderId}, Text: \"${it.messageText}\", Timestamp: ${it.timestamp}")
                        } ?: Log.w("ChatDoctorActivity", "Failed to convert document ${doc.id} to ChatMessage object. Check ChatMessage data class and Firestore fields.")
                    }
                    adapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                } else if (snapshots != null && snapshots.isEmpty) {
                    Log.d("ChatDoctorActivity", "No messages found for chat $chatId (snapshot is empty).")
                    messages.clear()
                    adapter.notifyDataSetChanged()
                } else {
                    Log.w("ChatDoctorActivity", "Messages snapshots is null for chat $chatId.")
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatListener?.remove()
        Log.d("ChatDoctorActivity", "ChatListener removed in onDestroy.")
    }
}