package com.example.eclinic.patientClasses

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.chat.ChatPatientActivity
import com.example.eclinic.doctorClasses.Doctor
import com.example.eclinic.firebase.Role
import com.example.eclinic.firebase.Specialization
import com.google.firebase.firestore.FirebaseFirestore

class DoctorsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var specializationSpinner: Spinner
    private lateinit var searchInput: EditText
    private lateinit var noResultsText: TextView

    private val doctorList = mutableListOf<Doctor>()
    private var fullDoctorList = listOf<Doctor>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_for_appointment_patient)

        initViews()
        setupRecyclerView()
        setupSpecializationFilter()
        setupSearchListener()
        fetchDoctorsFromFirestore(null)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        specializationSpinner = findViewById(R.id.specialization_spinner)
        searchInput = findViewById(R.id.search_input)
        noResultsText = findViewById(R.id.no_results_text)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        doctorAdapter = DoctorAdapter(
            doctorList,
            onDoctorClick = { selectedDoctor ->
                val intent = Intent(this, ChatPatientActivity::class.java).apply {
                    if (selectedDoctor.uid != null) {
                        putExtra("patientId", selectedDoctor.uid)
                        putExtra("patientName", "${selectedDoctor.firstName} ${selectedDoctor.lastName}")
                        Log.d("DoctorsListActivity", "Starting chat with Doctor: ${selectedDoctor.firstName} ${selectedDoctor.lastName}, UID: ${selectedDoctor.uid}")
                    } else {
                        Log.e("DoctorsListActivity", "Attempted to start chat with a doctor having null UID: ${selectedDoctor.firstName} ${selectedDoctor.lastName}")
                        Toast.makeText(this@DoctorsListActivity, "Error: Doctor ID missing to start chat.", Toast.LENGTH_SHORT).show()
                        return@DoctorAdapter
                    }
                }
                startActivity(intent)
            },
            onInfoClick = { selectedDoctor ->
                showDoctorInfoDialog(selectedDoctor)
            }
        )
        recyclerView.adapter = doctorAdapter
    }

    private fun setupSpecializationFilter() {
        val specializations = listOf("All") + Specialization.entries.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = adapter

        specializationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSpecialization = if (position == 0) null else Specialization.entries[position - 1].name
                fetchDoctorsFromFirestore(selectedSpecialization)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /* Not used */ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Not used */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                filterDoctors(query)
            }
        })
    }

    private fun filterDoctors(query: String) {
        val filtered = fullDoctorList.filter { doctor ->
            val fullName = "${doctor.firstName} ${doctor.lastName}".lowercase()
            val specializationDisplayName = Specialization.entries.find { it.name == doctor.specialization }?.displayName?.lowercase() ?: ""

            if (fullName.startsWith(query) || doctor.lastName.lowercase().startsWith(query)) return@filter true
            if (specializationDisplayName.contains(query)) return@filter true

            false
        }

        doctorList.clear()
        doctorList.addAll(filtered)
        doctorAdapter.notifyDataSetChanged()
        noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun fetchDoctorsFromFirestore(specializationFilter: String?) {
        var query = db.collection("users")
            .whereEqualTo("role", Role.DOCTOR.name)
            .whereEqualTo("verified", true)

        if (specializationFilter != null) {
            query = query.whereEqualTo("specialization", specializationFilter)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedDoctors = mutableListOf<Doctor>()
                for (doc in documents) {
                    val doctor = doc.toObject(Doctor::class.java)
                    doctor?.let {
                        if (it.role == Role.DOCTOR.name) {
                            fetchedDoctors.add(it)
                        }
                    } ?: Log.w("DoctorsListActivity", "Failed to parse doctor document: ${doc.id}")
                }
                fullDoctorList = fetchedDoctors.toList()
                filterDoctors(searchInput.text.toString().trim().lowercase())

                noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("DoctorsListActivity", "Error getting doctors: ${e.message}", e)
                Toast.makeText(this, "Error loading doctors: ${e.message}", Toast.LENGTH_LONG).show()
                noResultsText.visibility = View.VISIBLE
            }
    }

    private fun showDoctorInfoDialog(doctor: Doctor) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("${doctor.firstName} ${doctor.lastName}")
        builder.setMessage(
            "Title: ${doctor.title.ifEmpty { "N/A" }}\n" +
                    "Specialization: ${Specialization.entries.find { it.name == doctor.specialization }?.displayName ?: "N/A"}\n" +
                    "Workplace: ${doctor.workplace.ifEmpty { "N/A" }}\n" +
                    "PWZ Number: ${doctor.pwzNumber.ifEmpty { "N/A" }}\n\n" +
                    "Description:\n${doctor.bio.ifEmpty { "No description available." }}"
        )
        builder.setPositiveButton("Close", null)
        builder.show()
    }
}