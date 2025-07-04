package com.example.eclinic.patientClasses

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Prescription
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Activity responsible for displaying a list of prescriptions for the current patient.
 * Fetches prescriptions from Firestore and shows them in a RecyclerView.
 * Allows opening prescription PDFs in external PDF viewer apps.
 */
class PrescriptionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingBar: ProgressBar
    private lateinit var emptyText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prescriptionList = mutableListOf<Prescription>()

    /**
     * Initializes the activity, sets up the UI and triggers loading of prescriptions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescription)

        recyclerView = findViewById(R.id.prescriptionsRecyclerView)
        loadingBar = findViewById(R.id.loadingBar)
        emptyText = findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadPrescriptions()
    }

    /**
     * Loads prescriptions for the currently authenticated user from Firestore,
     * updating the UI depending on success or failure.
     */
    private fun loadPrescriptions() {
        val patientId = auth.currentUser?.uid ?: return

        loadingBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        recyclerView.visibility = View.GONE

        db.collection("prescriptions")
            .document(patientId)
            .collection("prescriptionsList")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                prescriptionList.clear()
                for (doc in result) {
                    val prescription = doc.toObject(Prescription::class.java)
                    prescriptionList.add(prescription)
                }

                loadingBar.visibility = View.GONE
                if (prescriptionList.isEmpty()) {
                    emptyText.text = "No prescriptions available"
                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.adapter = PrescriptionAdapter(prescriptionList) { prescription ->
                        openPdf(prescription.url)
                    }
                    recyclerView.visibility = View.VISIBLE
                    emptyText.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                loadingBar.visibility = View.GONE
                emptyText.text = "No prescriptions available"
                emptyText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Toast.makeText(this, "Failed to load prescriptions", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Attempts to open a PDF file from the given URL using an external PDF viewer.
     * Shows a Toast if no suitable viewer app is installed.
     *
     * @param url The URL to the PDF file to open.
     */
    private fun openPdf(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
        }
    }
}
