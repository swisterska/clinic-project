package com.example.eclinic.patientClasses

import PatientAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.chat.ChatDoctorActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

class PatientsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var patientAdapter: PatientAdapter
    private val conversationParticipants = mutableListOf<Patient>()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients_list)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserId = auth.currentUser?.uid ?: run {
            Log.e("PatientsListActivity", "Current user ID is null. User not authenticated. Finishing activity.")
            finish()
            return
        }
        Log.d("PatientsListActivity", "Current User ID: $currentUserId")

        recyclerView = findViewById(R.id.patients_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        patientAdapter = PatientAdapter(
            conversationParticipants,
            onPatientClick = { selectedPatient ->
                val intent = Intent(this, ChatDoctorActivity::class.java)
                intent.putExtra("patientId", selectedPatient.uid)
                intent.putExtra("patientName", "${selectedPatient.firstName} ${selectedPatient.lastName}")
                startActivity(intent)
            }
        )
        recyclerView.adapter = patientAdapter

        fetchConversations()
    }

    private fun fetchConversations() {
        Log.d("PatientsListActivity", "Attempting to fetch conversations for user: $currentUserId")

        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                conversationParticipants.clear()
                val participantIds = mutableSetOf<String>()

                if (querySnapshot.isEmpty) {
                    Log.d("PatientsListActivity", "Query snapshot is empty. No chat documents found matching criteria.")
                    Toast.makeText(this, "No active conversations.", Toast.LENGTH_SHORT).show()
                    patientAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                Log.d("PatientsListActivity", "Found ${querySnapshot.documents.size} chat documents.")
                for (document in querySnapshot.documents) {
                    val participants = document.get("participants") as? List<String>
                    Log.d("PatientsListActivity", "Processing chat document ID: ${document.id}")
                    Log.d("PatientsListActivity", "Participants field raw value: ${document.get("participants")}")
                    Log.d("PatientsListActivity", "Participants list (after cast): $participants")

                    if (participants != null) {
                        val otherParticipantId = participants.find { it != currentUserId }
                        Log.d("PatientsListActivity", "Other participant ID: $otherParticipantId")

                        if (otherParticipantId != null && !participantIds.contains(otherParticipantId)) {
                            participantIds.add(otherParticipantId)
                            Log.d("PatientsListActivity", "Fetching details for other participant ID: $otherParticipantId")

                            firestore.collection("users").document(otherParticipantId)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    Log.d("PatientsListActivity", "Fetched user document for ID: ${userDoc.id}. Exists: ${userDoc.exists()}")
                                    if (userDoc.exists()) {
                                        val patient = userDoc.toObject(Patient::class.java)
                                        if (patient != null) {
                                            conversationParticipants.add(patient)
                                            patientAdapter.notifyDataSetChanged()
                                            Log.d("PatientsListActivity", "Added patient: ${patient.firstName} ${patient.lastName} (UID: ${patient.uid})")
                                        } else {
                                            Log.w("PatientsListActivity", "Failed to convert user document ${userDoc.id} to Patient object. Check Patient class and Firestore data.")
                                        }
                                    } else {
                                        Log.w("PatientsListActivity", "User document ${userDoc.id} does not exist in 'users' collection.")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("PatientsListActivity", "Error fetching participant details for $otherParticipantId: ${e.message}", e)
                                }
                        } else if (otherParticipantId == null) {
                            Log.d("PatientsListActivity", "Only one participant in chat document ${document.id} (or participant is current user). Skipping.")
                        } else {
                            Log.d("PatientsListActivity", "Participant $otherParticipantId already processed. Skipping.")
                        }
                    } else {
                        Log.w("PatientsListActivity", "Participants field is null or not a List<String> in chat document ${document.id}. Check Firestore data structure.")
                    }
                }

                if (conversationParticipants.isEmpty() && participantIds.isNotEmpty()) {
                    Log.w("PatientsListActivity", "Some chat documents found, but no valid conversation participants could be loaded.")
                    Toast.makeText(this, "Some conversations could not be loaded.", Toast.LENGTH_SHORT).show()
                } else if (conversationParticipants.isEmpty() && participantIds.isEmpty() && !querySnapshot.isEmpty) {
                    Log.w("PatientsListActivity", "Chat documents found, but no other participants were identified or processed.")
                    Toast.makeText(this, "No suitable conversation participants found.", Toast.LENGTH_SHORT).show()
                } else if (conversationParticipants.isNotEmpty()) {
                    Log.d("PatientsListActivity", "Successfully loaded ${conversationParticipants.size} conversations.")
                }

            }
            .addOnFailureListener { e ->
                Log.e("PatientsListActivity", "Error getting conversations: ", e)
                Toast.makeText(this, "Error loading conversations: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}