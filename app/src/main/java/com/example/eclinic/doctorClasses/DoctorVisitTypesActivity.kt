package com.example.eclinic.doctorClasses

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eclinic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.SetOptions

class DoctorVisitTypesActivity : AppCompatActivity() {

    private lateinit var visitNameEditText: EditText
    private lateinit var visitPriceEditText: EditText
    private lateinit var addVisitButton: Button
    private lateinit var visitsRecyclerView: RecyclerView
    private lateinit var noVisitsTextView: TextView

    private lateinit var adapter: VisitTypeAdapter
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_visit_types)

        visitNameEditText = findViewById(R.id.visitNameEditText)
        visitPriceEditText = findViewById(R.id.visitPriceEditText)
        addVisitButton = findViewById(R.id.addVisitButton)
        visitsRecyclerView = findViewById(R.id.visitTypesRecyclerView)
        noVisitsTextView = findViewById(R.id.noVisitsTextView)

        adapter = VisitTypeAdapter(listOf(), { name, price ->
            visitNameEditText.setText(name)
            visitPriceEditText.setText(price)
        }, { name ->
            deleteVisitType(name)
        })


        visitsRecyclerView.layoutManager = LinearLayoutManager(this)
        visitsRecyclerView.adapter = adapter

        addVisitButton.setOnClickListener {
            val name = visitNameEditText.text.toString().trim()
            val price = visitPriceEditText.text.toString().trim()
            if (name.isNotEmpty() && price.isNotEmpty()) {
                addVisitType(name, price)
            }
        }

        loadVisitTypes()
    }

    private fun addVisitType(name: String, price: String) {
        val docRef = db.collection("visitTypes").document(uid)
        docRef.update(name, price)
            .addOnSuccessListener {
                visitNameEditText.text.clear()
                visitPriceEditText.text.clear()
                loadVisitTypes()
            }
            .addOnFailureListener {
                // Create if not exists
                docRef.set(mapOf(name to price), SetOptions.merge())
                    .addOnSuccessListener { loadVisitTypes() }
            }
    }

    private fun deleteVisitType(name: String) {
        val updates = hashMapOf<String, Any>(
            name to com.google.firebase.firestore.FieldValue.delete()
        )
        db.collection("visitTypes").document(uid)
            .update(updates)
            .addOnSuccessListener { loadVisitTypes() }
    }

    private fun loadVisitTypes() {
        db.collection("visitTypes").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data?.map { it.key to it.value.toString() } ?: emptyList()
                adapter.updateList(data)
                noVisitsTextView.visibility = if (data.isEmpty()) TextView.VISIBLE else TextView.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load visit types", Toast.LENGTH_SHORT).show()
            }
    }
}
