package com.example.eclinic.doctorClasses

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.SetOptions

/**
 * Activity for doctors to manage their types of visits and associated prices.
 * Doctors can add, edit, and delete different visit types, which are stored in Firebase Firestore.
 */
class DoctorVisitTypesActivity : AppCompatActivity() {

    private lateinit var visitNameEditText: EditText
    private lateinit var visitPriceEditText: EditText
    private lateinit var addVisitButton: Button
    private lateinit var visitsRecyclerView: RecyclerView
    private lateinit var noVisitsTextView: TextView

    private lateinit var adapter: VisitTypeAdapter
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the RecyclerView adapter,
     * configures click listeners, and loads existing visit types.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_visit_types)

        visitNameEditText = findViewById(R.id.visitNameEditText)
        visitPriceEditText = findViewById(R.id.visitPriceEditText)
        addVisitButton = findViewById(R.id.addVisitButton)
        visitsRecyclerView = findViewById(R.id.visitTypesRecyclerView)
        noVisitsTextView = findViewById(R.id.noVisitsTextView)

        adapter = VisitTypeAdapter(
            listOf(), // Initial empty list
            { name, price -> showEditDialog(name, price) }, // Lambda for edit action
            { name -> deleteVisitType(name) } // Lambda for delete action
        )

        visitsRecyclerView.layoutManager = LinearLayoutManager(this)
        visitsRecyclerView.adapter = adapter

        addVisitButton.setOnClickListener {
            val name = visitNameEditText.text.toString().trim()
            val price = visitPriceEditText.text.toString().trim()
            if (name.isNotEmpty() && price.isNotEmpty()) {
                addVisitType(name, price)
            } else {
                Toast.makeText(this, "Please enter both name and price.", Toast.LENGTH_SHORT).show()
            }
        }

        loadVisitTypes()
    }

    /**
     * Adds a new visit type or updates an existing one in Firestore.
     * If the document (containing visit types for the current doctor) exists, it attempts to update it.
     * If the document does not exist, it creates a new one with the given visit type.
     * @param name The name of the visit type (e.g., "Standard Consultation").
     * @param price The price of the visit type (e.g., "150 PLN").
     */
    private fun addVisitType(name: String, price: String) {
        val docRef = db.collection("visitTypes").document(uid)
        docRef.update(name, price) // Attempt to update (if document exists)
            .addOnSuccessListener {
                Toast.makeText(this, "Visit type added/updated.", Toast.LENGTH_SHORT).show()
                visitNameEditText.text.clear()
                visitPriceEditText.text.clear()
                loadVisitTypes() // Reload to reflect changes
            }
            .addOnFailureListener {
                // If update fails (e.g., document doesn't exist), try to create/merge
                docRef.set(mapOf(name to price), SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Visit type added.", Toast.LENGTH_SHORT).show()
                        visitNameEditText.text.clear()
                        visitPriceEditText.text.clear()
                        loadVisitTypes() // Reload to reflect changes
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add visit type: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("DoctorVisitTypesActivity", "Error adding visit type", e)
                    }
            }
    }

    /**
     * Deletes a specified visit type from the current doctor's visit types in Firestore.
     * @param name The name of the visit type to be deleted.
     */
    private fun deleteVisitType(name: String) {
        val updates = hashMapOf<String, Any>(
            name to com.google.firebase.firestore.FieldValue.delete()
        )
        db.collection("visitTypes").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Visit type deleted.", Toast.LENGTH_SHORT).show()
                loadVisitTypes() // Reload to reflect changes
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete visit type: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DoctorVisitTypesActivity", "Error deleting visit type", e)
            }
    }

    /**
     * Loads all visit types for the current doctor from Firestore.
     * Updates the RecyclerView adapter with the retrieved data and manages the visibility
     * of the "no visits" message based on whether any visit types are found.
     */
    private fun loadVisitTypes() {
        db.collection("visitTypes").document(uid)
            .get()
            .addOnSuccessListener { document ->
                // Convert document data to a list of pairs (name to price string)
                val data = document.data?.map { it.key to it.value.toString() } ?: emptyList()
                adapter.updateList(data) // Update the adapter with new data
                // Show/hide "no visits" message
                noVisitsTextView.visibility = if (data.isEmpty()) TextView.VISIBLE else TextView.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load visit types.", Toast.LENGTH_SHORT).show()
                Log.e("DoctorVisitTypesActivity", "Error loading visit types", e)
            }
    }

    /**
     * Displays an AlertDialog allowing the doctor to edit an existing visit type's name and price.
     * @param oldName The current name of the visit type to be edited.
     * @param oldPrice The current price of the visit type to be edited.
     */
    private fun showEditDialog(oldName: String, oldPrice: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_visit_type, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.editVisitName)
        val priceEdit = dialogView.findViewById<EditText>(R.id.editVisitPrice)

        nameEdit.setText(oldName)
        priceEdit.setText(oldPrice)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Visit Type")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameEdit.text.toString().trim()
                val newPrice = priceEdit.text.toString().trim()
                if (newName.isNotEmpty() && newPrice.isNotEmpty()) {
                    updateVisitType(oldName, newName, newPrice)
                } else {
                    Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null) // Do nothing on cancel
            .create()

        dialog.show()
    }

    /**
     * Updates an existing visit type in Firestore.
     * This involves deleting the old entry and then adding the new (potentially updated) entry.
     * @param oldName The original name of the visit type before editing.
     * @param newName The new name for the visit type.
     * @param newPrice The new price for the visit type.
     */
    private fun updateVisitType(oldName: String, newName: String, newPrice: String) {
        val docRef = db.collection("visitTypes").document(uid)

        // First, delete the old entry using the old name as the field key
        val updates = hashMapOf<String, Any>(
            oldName to com.google.firebase.firestore.FieldValue.delete()
        )

        docRef.update(updates)
            .addOnSuccessListener {
                // On successful deletion, add the new entry (or update if newName is same as oldName)
                docRef.set(mapOf(newName to newPrice), SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Updated successfully.", Toast.LENGTH_SHORT).show()
                        loadVisitTypes() // Reload to reflect changes
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("DoctorVisitTypesActivity", "Error updating new visit type after deletion", e)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete old visit type during update: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DoctorVisitTypesActivity", "Error deleting old visit type during update", e)
            }
    }
}