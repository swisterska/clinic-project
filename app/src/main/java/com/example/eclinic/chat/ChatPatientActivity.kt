package com.example.eclinic.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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
import com.google.firebase.storage.FirebaseStorage

/**
 * Activity for patients to engage in a chat conversation with a specific other participant (e.g., a doctor).
 * This activity handles sending and receiving messages in real-time using Firebase Firestore,
 * ensuring messages are displayed chronologically and scroll to the latest message.
 */
class ChatPatientActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var inputEditText: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var otherParticipantNameTextView: TextView // This is not used in the layout for now, consider removing or adding.

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var currentUserId: String = ""
    private var otherParticipantId: String = ""
    private var otherParticipantName: String = ""
    private var chatId: String? = null

    private var chatListener: ListenerRegistration? = null

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up Firebase instances, retrieves user and other participant IDs
     * from the intent, configures the RecyclerView, and sets up message sending functionality.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_doctor) // Note: Using chat_doctor layout, ensure it's suitable for patient chat.

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val attachFileButton = findViewById<ImageButton>(R.id.button_attach_file_doc)
        attachFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE)
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserId = auth.currentUser?.uid ?: run {
            Log.e("ChatActivity", "Current user ID is null. User not authenticated. Finishing activity.")
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("ChatActivity", "Current User ID (logged in): $currentUserId")

        // 'patientId' extra is used here as a generic 'otherParticipantId'
        otherParticipantId = intent.getStringExtra("patientId") ?: run {
            Log.e("ChatActivity", "otherParticipantId not provided in Intent. Finishing activity.")
            Toast.makeText(this, "Error: Other chat participant ID missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        otherParticipantName = intent.getStringExtra("patientName") ?: "Unknown User"
        Log.d("ChatActivity", "Other participant ID: $otherParticipantId")
        Log.d("ChatActivity", "Other participant Name: $otherParticipantName")

        // Set the action bar title to the other participant's name
        supportActionBar?.title = otherParticipantName

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

    /**
     * Finds an existing chat document between the current user and the other participant,
     * or creates a new one if it doesn't exist. The chat ID is generated by sorting
     * the two participant IDs to ensure a consistent ID regardless of who initiates the chat.
     * Once found/created, it starts listening for messages.
     */
    private fun findOrCreateChat() {
        val participantsSorted = listOf(currentUserId, otherParticipantId).sorted()
        val generatedChatId = "${participantsSorted[0]}_${participantsSorted[1]}"
        Log.d("ChatActivity", "Generated chat ID (sorted): $generatedChatId")

        firestore.collection("chats").document(generatedChatId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    chatId = generatedChatId
                    Log.d("ChatActivity", "Found existing chat with ID: $chatId")
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
                            Log.d("ChatActivity", "Created new chat with ID: $chatId")
                            listenForMessages()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatActivity", "Error creating chat with ID: $generatedChatId", e)
                            Toast.makeText(this, "Error creating chat: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Error finding or creating chat document: ${e.message}", e)
                Toast.makeText(this, "Error finding chat: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Sends a new chat message to the Firestore database.
     * The message is added to the "messages" subcollection of the current chat.
     * It also updates the last message timestamp and text in the main chat document.
     * @param text The content of the message to be sent.
     */
    private fun sendMessage(text: String) {
        if (chatId == null) {
            Log.e("ChatActivity", "Chat ID is null, cannot send message. Re-finding chat...")
            Toast.makeText(this, "Chat not ready yet, please try again.", Toast.LENGTH_SHORT).show()
            findOrCreateChat() // Attempt to re-find/create chat if ID is null
            return
        }

        val chatMessage = ChatMessage(
            senderId = currentUserId,
            receiverId = otherParticipantId,
            messageText = text
        )

        firestore.collection("chats").document(chatId!!)
            .collection("messages")
            .add(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Message sent successfully to chat $chatId.")
                // Update lastMessageTimestamp and lastMessageText in the main chat document
                firestore.collection("chats").document(chatId!!)
                    .update(
                        "lastMessageTimestamp", FieldValue.serverTimestamp(),
                        "lastMessageText", text
                    )
                    .addOnSuccessListener {
                        Log.d("ChatActivity", "Chat metadata (timestamp, text) updated.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatActivity", "Error updating chat metadata: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Error sending message: ${e.message}", e)
                Toast.makeText(this, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Sets up a real-time listener for messages in the current chat.
     * Messages are retrieved from Firestore, ordered by timestamp, and displayed in the RecyclerView.
     * The RecyclerView automatically scrolls to the latest message.
     */
    private fun listenForMessages() {
        if (chatId == null) {
            Log.e("ChatActivity", "Chat ID is null, cannot listen for messages. Waiting for chat to be found/created.")
            return
        }

        // Remove any existing listener to prevent duplicate listeners
        chatListener?.remove()

        Log.d("ChatActivity", "Starting to listen for messages in chat: $chatId")

        chatListener = firestore.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Order messages chronologically
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatActivity", "Listen for messages failed.", e)
                    Toast.makeText(this, "Error loading messages: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    Log.d("ChatActivity", "Received ${snapshots.documents.size} messages for chat $chatId.")
                    messages.clear() // Clear existing messages before adding new ones
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(ChatMessage::class.java)
                        message?.let {
                            messages.add(it)
                            Log.d("ChatActivity", "Message loaded: Sender: ${it.senderId}, Text: \"${it.messageText}\", Timestamp: ${it.timestamp}")
                        } ?: Log.w("ChatActivity", "Failed to convert document ${doc.id} to ChatMessage object. Check ChatMessage data class and Firestore fields.")
                    }
                    adapter.notifyDataSetChanged() // Notify adapter of data change
                    if (messages.isNotEmpty()) {
                        recyclerView.scrollToPosition(messages.size - 1) // Scroll to the bottom
                    }
                } else if (snapshots != null && snapshots.isEmpty) {
                    Log.d("ChatActivity", "No messages found for chat $chatId (snapshot is empty).")
                    messages.clear() // Clear messages if snapshot is empty (e.g., chat was just created)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.w("ChatActivity", "Messages snapshots is null for chat $chatId.")
                }
            }
    }

    /**
     * Called when the activity is about to be destroyed.
     * Removes the Firestore snapshot listener to prevent memory leaks and unnecessary data fetching.
     */
    override fun onDestroy() {
        super.onDestroy()
        chatListener?.remove()
        Log.d("ChatActivity", "ChatListener removed in onDestroy.")
    }

    /**
     * Sends a file message in the current chat session.
     *
     * Creates a chat message of type "file" with a clickable hyperlink to the uploaded file.
     * If the chat ID is not yet initialized, the function attempts to create the chat first.
     *
     * @param fileUrl The publicly accessible URL of the uploaded file.
     */
    private fun sendFileMessage(fileUrl: String) {
        if (chatId == null) {
            Log.w("ChatActivity", "Chat ID jest null podczas próby wysłania wiadomości z plikiem.")
            Toast.makeText(this, "Chat jeszcze nie gotowy, spróbuj ponownie.", Toast.LENGTH_SHORT).show()
            findOrCreateChat()
            return
        }

        val clickableLinkText = "<a href=\"$fileUrl\">View the file</a>"

        val chatMessage = ChatMessage(
            senderId = currentUserId,
            receiverId = otherParticipantId,
            messageText = clickableLinkText,
            messageType = "file",
            fileUrl = fileUrl
        )

        firestore.collection("chats").document(chatId!!)
            .collection("messages")
            .add(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Wiadomość plikowa wysłana pomyślnie.")
                // Aktualizacja metadanych chatu
                firestore.collection("chats").document(chatId!!)
                    .update(
                        "lastMessageTimestamp", FieldValue.serverTimestamp(),
                        "lastMessageText", "[Plik]"
                    )
                    .addOnSuccessListener {
                        Log.d("ChatActivity", "Metadane chatu zaktualizowane po wysłaniu pliku.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatActivity", "Błąd aktualizacji metadanych chatu: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Błąd wysyłania wiadomości plikowej: ${e.message}", e)
                Toast.makeText(this, "Błąd wysyłania wiadomości plikowej: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Holds static constants used within the activity.
     */
    companion object {
        /**
         * Request code used to identify the file picker activity result.
         */
        private const val PICK_FILE_REQUEST_CODE = 1001
    }

    /**
     * Handles the result of the file picker activity.
     *
     * If a file was selected successfully, it proceeds with uploading it to Firebase Storage.
     *
     * @param requestCode The request code used when starting the file picker intent.
     * @param resultCode The result status code (e.g., RESULT_OK).
     * @param data The returned intent containing the selected file's URI.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                uploadFileToFirebaseStorage(uri)
            }
        }
    }

    /**
     * Uploads the selected file to Firebase Storage and, upon success,
     * retrieves its download URL to send it as a chat message.
     *
     * If the chat is not yet ready (`chatId` is null), it informs the user.
     *
     * @param fileUri The URI of the file selected by the user.
     */
    private fun uploadFileToFirebaseStorage(fileUri: Uri) {

        if (chatId == null) {
            Toast.makeText(this, "Chat nie jest gotowy, spróbuj ponownie później.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("chat_files/${System.currentTimeMillis()}_${fileUri.lastPathSegment}")

        val uploadTask = fileRef.putFile(fileUri)
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                Log.d("ChatActivity", "Upload success, download URL: $downloadUrl")
                sendFileMessage(downloadUrl.toString())
            }.addOnFailureListener { e ->
                Log.e("ChatActivity", "Failed to get download URL: ${e.message}", e)
                Toast.makeText(this, "Nie udało się pobrać linku pliku: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("ChatActivity", "Upload failed: ${e.message}", e)
            Toast.makeText(this, "Upload pliku nie powiódł się: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}