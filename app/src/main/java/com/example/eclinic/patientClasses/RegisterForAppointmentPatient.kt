package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Doctor
import com.example.eclinic.firebase.Role
import com.example.eclinic.firebase.Specialization
import com.example.eclinic.firebase.User
import com.google.firebase.firestore.FirebaseFirestore

class RegisterForAppointmentPatient : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var specializationSpinner: Spinner
    private lateinit var searchInput: EditText
    private lateinit var noResultsText: TextView


    private val doctorList = mutableListOf<Doctor>()
    private var fullDoctorList = listOf<Doctor>() // backup for search
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_for_appointment_patient)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        specializationSpinner = findViewById(R.id.specialization_spinner)
        searchInput = findViewById(R.id.search_input)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        doctorAdapter = DoctorAdapter(
            doctorList,
            onDoctorClick = { selectedDoctor ->
                val intent = Intent(this, VisitTypeActivity::class.java)
                intent.putExtra("specialization", selectedDoctor.description)
                intent.putExtra("doctorName", selectedDoctor.name)
                startActivity(intent)
            },
            onInfoClick = { selectedDoctor ->
                showDoctorInfoDialog(selectedDoctor)
            }
        )

        recyclerView.adapter = doctorAdapter

        // Setup filtering
        setupSpecializationFilter()
        setupSearchListener()

        // Load doctors (all initially)
        fetchDoctorsFromFirestore(null)

        noResultsText = findViewById(R.id.no_results_text)

    }

    private fun setupSpecializationFilter() {
        val specializations = listOf("All") + Specialization.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = adapter

        specializationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSpecialization = if (position == 0) null else specializations[position]
                fetchDoctorsFromFirestore(selectedSpecialization)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                // First filter: Check for exact matches (startsWith, or startsWith for each word)
                val filtered = fullDoctorList.filter { doctor ->
                    val name = doctor.name.lowercase()
                    val parts = name.split(" ")

                    // Match if query is a prefix of any word (exact match)
                    if (name.startsWith(query) || parts.any { it.startsWith(query) }) return@filter true

                    // Allow fuzzy matches (typos), but only after no prefix match
                    parts.any { typos(it, query) <= 2 }
                }

                // Update the list and notify adapter
                doctorList.clear()
                doctorList.addAll(filtered)
                doctorAdapter.notifyDataSetChanged()

                // Show 'no results' message if no doctors matched
                noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE
            }
        })
    }


    private fun fetchDoctorsFromFirestore(specialization: String?) {
        var query = db.collection("users").whereEqualTo("role", Role.DOCTOR.name)

        if (specialization != null) {
            query = query.whereEqualTo("specialization", specialization)
        }

        query.get()
            .addOnSuccessListener { documents ->
                doctorList.clear()
                for (doc in documents) {
                    val data = doc.data
                    val doctor = User.fromMap(data)

                    // Get description from Firestore (if available)
                    val description = doc.getString("description") ?: "Experienced in ${doctor.specialization}"

                    doctorList.add(
                        Doctor(
                            name = "${doctor.firstName} ${doctor.lastName}",
                            bio = "",
                            description = doctor.specialization,
                            profilePicUrl = doctor.profilePictureUrl
                        )
                    )
                }
                fullDoctorList = doctorList.toList()
                doctorAdapter.notifyDataSetChanged()

                noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE

            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting doctors", e)
            }
    }

    private fun typos(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,    // deletion
                    dp[i][j - 1] + 1,    // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[a.length][b.length]
    }

    private fun showDoctorInfoDialog(doctor: Doctor) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(doctor.name)
        builder.setMessage(
            "Specialization: ${doctor.description}\n\n" +
                    "Bio: ${doctor.bio.ifEmpty { "No bio available." }}"
        )
        builder.setPositiveButton("Close", null)
        builder.show()
    }


}
