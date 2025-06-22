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

/**
 * [DoctorsListActivity] displays a list of verified doctors for patients to view.
 * It provides functionalities to filter doctors by specialization and search by name.
 * Patients can click on a doctor to initiate a chat or view more detailed information.
 */
class DoctorsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var specializationSpinner: Spinner
    private lateinit var searchInput: EditText
    private lateinit var noResultsText: TextView

    private val doctorList = mutableListOf<Doctor>() // Currently displayed doctors
    private var fullDoctorList = listOf<Doctor>() // All fetched doctors before filtering/searching

    private val db = FirebaseFirestore.getInstance() // Firebase Firestore instance

    /**
     * Called when the activity is first created.
     * Initializes views, sets up the RecyclerView, specialization filter, search listener,
     * and fetches the initial list of doctors from Firestore.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_for_appointment_patient)

        initViews()
        setupRecyclerView()
        setupSpecializationFilter()
        setupSearchListener()
        fetchDoctorsFromFirestore(null) // Load all doctors initially
    }

    /**
     * Initializes UI components by finding them by their IDs from the layout.
     */
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        specializationSpinner = findViewById(R.id.specialization_spinner)
        searchInput = findViewById(R.id.search_input)
        noResultsText = findViewById(R.id.no_results_text)
    }

    /**
     * Sets up the RecyclerView with a [LinearLayoutManager] and initializes the [DoctorAdapter].
     * Defines click listeners for doctor items (to start chat) and info icons (to show detailed info).
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        doctorAdapter = DoctorAdapter(
            doctorList,
            onDoctorClick = { selectedDoctor ->
                // Handle click on a doctor item to start a chat
                val intent = Intent(this, ChatPatientActivity::class.java).apply {
                    if (selectedDoctor.uid != null) {
                        putExtra("patientId", selectedDoctor.uid) // Pass doctor's UID as patientId for chat context
                        putExtra("patientName", "${selectedDoctor.firstName} ${selectedDoctor.lastName}") // Pass doctor's name
                        Log.d("DoctorsListActivity", "Starting chat with Doctor: ${selectedDoctor.firstName} ${selectedDoctor.lastName}, UID: ${selectedDoctor.uid}")
                    } else {
                        Log.e("DoctorsListActivity", "Attempted to start chat with a doctor having null UID: ${selectedDoctor.firstName} ${selectedDoctor.lastName}")
                        Toast.makeText(this@DoctorsListActivity, "Error: Doctor ID missing to start chat.", Toast.LENGTH_SHORT).show()
                        return@DoctorAdapter // Exit the lambda if UID is null
                    }
                }
                startActivity(intent)
            },
            onInfoClick = { selectedDoctor ->
                // Handle click on the info icon to show doctor details
                showDoctorInfoDialog(selectedDoctor)
            }
        )
        recyclerView.adapter = doctorAdapter
    }

    /**
     * Sets up the specialization filter spinner.
     * Populates the spinner with "All" and all available [Specialization] display names.
     * Sets an [AdapterView.OnItemSelectedListener] to filter doctors when a specialization is selected.
     */
    private fun setupSpecializationFilter() {
        // Create a list of specializations, starting with "All"
        val specializations = listOf("All") + Specialization.entries.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = adapter

        specializationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // If "All" is selected (position 0), specializationFilter is null, otherwise get the enum name
                val selectedSpecialization = if (position == 0) null else Specialization.entries[position - 1].name
                fetchDoctorsFromFirestore(selectedSpecialization) // Refetch doctors based on selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No operation needed when nothing is selected
            }
        }
    }

    /**
     * Sets up a [TextWatcher] for the search input field.
     * It triggers the [filterDoctors] method every time the text in the search input changes,
     * allowing for real-time search functionality.
     */
    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /* Not used */ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Not used */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                filterDoctors(query) // Filter the list based on the current search query
            }
        })
    }

    /**
     * Filters the [fullDoctorList] based on the provided search query.
     * Doctors are filtered by matching the query against their full name (first or last name starts with query)
     * or their specialization display name (contains query).
     * The `doctorList` (displayed list) is updated, and the RecyclerView is notified of the data change.
     * The visibility of `noResultsText` is updated accordingly.
     * @param query The search string (case-insensitive, trimmed) used to filter doctors.
     */
    private fun filterDoctors(query: String) {
        val filtered = fullDoctorList.filter { doctor ->
            val fullName = "${doctor.firstName} ${doctor.lastName}".lowercase()
            // Get the display name for specialization to allow searching by display name
            val specializationDisplayName = Specialization.entries.find { it.name == doctor.specialization }?.displayName?.lowercase() ?: ""

            // Check if full name or last name starts with the query
            if (fullName.startsWith(query) || doctor.lastName.lowercase().startsWith(query)) return@filter true
            // Check if specialization display name contains the query
            if (specializationDisplayName.contains(query)) return@filter true

            false // Exclude if no match
        }

        doctorList.clear()
        doctorList.addAll(filtered)
        doctorAdapter.notifyDataSetChanged() // Notify adapter that data has changed
        noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE // Show/hide no results message
    }

    /**
     * Fetches verified doctor data from Firebase Firestore.
     * Optionally filters by a specific specialization.
     * On success, updates `fullDoctorList` and then calls [filterDoctors] to apply current search/filter.
     * On failure, logs the error and shows a toast message.
     * @param specializationFilter (Optional) The name of the specialization to filter by (e.g., "CARDIOLOGIST").
     * If null, all verified doctors are fetched.
     */
    private fun fetchDoctorsFromFirestore(specializationFilter: String?) {
        var query = db.collection("users")
            .whereEqualTo("role", Role.DOCTOR.name) // Only fetch documents with role "DOCTOR"
            .whereEqualTo("verified", true) // Only fetch verified doctors

        if (specializationFilter != null) {
            query = query.whereEqualTo("specialization", specializationFilter) // Add specialization filter if provided
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedDoctors = mutableListOf<Doctor>()
                for (doc in documents) {
                    val doctor = doc.toObject(Doctor::class.java) // Convert document to Doctor object
                    doctor?.let {
                        // Double-check the role, though already filtered by query
                        if (it.role == Role.DOCTOR.name) {
                            fetchedDoctors.add(it)
                        }
                    } ?: Log.w("DoctorsListActivity", "Failed to parse doctor document: ${doc.id}")
                }
                fullDoctorList = fetchedDoctors.toList() // Update the full list of doctors
                // Apply the current search filter to the newly fetched data
                filterDoctors(searchInput.text.toString().trim().lowercase())

                noResultsText.visibility = if (doctorList.isEmpty()) View.VISIBLE else View.GONE // Update no results text visibility
            }
            .addOnFailureListener { e ->
                Log.e("DoctorsListActivity", "Error getting doctors: ${e.message}", e)
                Toast.makeText(this, "Error loading doctors: ${e.message}", Toast.LENGTH_LONG).show()
                noResultsText.visibility = View.VISIBLE // Show no results on failure
            }
    }

    /**
     * Displays an [AlertDialog] showing detailed information about a selected doctor.
     * Includes title, specialization, workplace, PWZ number, and bio.
     * @param doctor The [Doctor] object whose information is to be displayed.
     */
    private fun showDoctorInfoDialog(doctor: Doctor) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("${doctor.firstName} ${doctor.lastName}") // Dialog title: Doctor's Full Name
        builder.setMessage(
            "Title: ${doctor.title.ifEmpty { "N/A" }}\n" + // Display title, "N/A" if empty
                    "Specialization: ${Specialization.entries.find { it.name == doctor.specialization }?.displayName ?: "N/A"}\n" + // Convert specialization name to display name
                    "Workplace: ${doctor.workplace.ifEmpty { "N/A" }}\n" + // Display workplace, "N/A" if empty
                    "PWZ Number: ${doctor.pwzNumber.ifEmpty { "N/A" }}\n\n" + // Display PWZ number, "N/A" if empty
                    "Description:\n${doctor.bio.ifEmpty { "No description available." }}" // Display bio, or a message if empty
        )
        builder.setPositiveButton("Close", null) // Simple close button
        builder.show() // Show the dialog
    }
}