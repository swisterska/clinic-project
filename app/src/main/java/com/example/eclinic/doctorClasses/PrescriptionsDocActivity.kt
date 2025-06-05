package com.example.eclinic.doctorClasses

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrescriptionsDocActivity : AppCompatActivity() {

    private lateinit var patientSpinner: Spinner
    private lateinit var medicationName: EditText
    private lateinit var dosage: EditText
    private lateinit var units: EditText
    private lateinit var comments: EditText
    private lateinit var submitButton: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val patientMap = mutableMapOf<String, String>() // name -> id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescriptions_doc)

        patientSpinner = findViewById(R.id.patientSpinner)
        medicationName = findViewById(R.id.medicationName)
        dosage = findViewById(R.id.dosage)
        units = findViewById(R.id.units)
        comments = findViewById(R.id.comments)
        submitButton = findViewById(R.id.submitButton)

        loadPatients()

        submitButton.setOnClickListener {
            generateAndUploadPrescription()
        }
    }

    private fun loadPatients() {
        val doctorId = auth.currentUser?.uid ?: return

        db.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .get()
            .addOnSuccessListener { result ->
                val patientNames = mutableListOf<String>()
                for (doc in result) {
                    val name = doc.getString("patientName") ?: continue
                    val id = doc.getString("patientId") ?: continue
                    if (!patientMap.containsKey(name)) {
                        patientNames.add(name)
                        patientMap[name] = id
                    }
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, patientNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                patientSpinner.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load patients", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateAndUploadPrescription() {
        val selectedPatientName = patientSpinner.selectedItem?.toString() ?: return
        val patientId = patientMap[selectedPatientName] ?: return
        val medication = medicationName.text.toString().trim()
        val dose = dosage.text.toString().trim()
        val units = units.text.toString().trim()
        val comment = comments.text.toString().trim()
        val doctorId = auth.currentUser?.uid ?: "Unknown"

        if (medication.isEmpty() || dose.isEmpty() || units.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "Prescription_${System.currentTimeMillis()}.pdf"
        val file = File(cacheDir, fileName)

        try {
            generateStyledPdf(
                file = file,
                patientName = selectedPatientName,
                medication = medication,
                dosage = dose,
                units = units,
                comments = comment
            )
            uploadPdfToFirebase(file, doctorId, patientId)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateStyledPdf(
        file: File,
        patientName: String,
        medication: String,
        dosage: String,
        units: String,
        comments: String
    ) {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0D47A1")
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 3f, 3f, Color.GRAY)
        }
        canvas.drawText("Prescription", (pageWidth / 2).toFloat(), 80f, titlePaint)

        val bodyPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 16f
            isAntiAlias = true
        }

        var yPosition = 130f

        fun drawLine(label: String, value: String) {
            val labelPaint = TextPaint(bodyPaint).apply {
                isFakeBoldText = true
                color = Color.DKGRAY
            }
            canvas.drawText(label, margin.toFloat(), yPosition, labelPaint)
            canvas.drawText(value, margin + 150f, yPosition, bodyPaint)
            yPosition += 30f
        }

        drawLine("Patient:", patientName)
        drawLine("Medication:", medication)
        drawLine("Dosage:", dosage)
        drawLine("Units:", units)

        yPosition += 10f
        val commentsTitle = "Comments:"
        val commentsTitlePaint = TextPaint(bodyPaint).apply {
            isFakeBoldText = true
            color = Color.DKGRAY
        }
        canvas.drawText(commentsTitle, margin.toFloat(), yPosition, commentsTitlePaint)
        yPosition += 25f

        val commentWidth = pageWidth - 2 * margin
        val staticLayout = StaticLayout.Builder.obtain(comments, 0, comments.length, bodyPaint, commentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1.2f)
            .build()

        canvas.save()
        canvas.translate(margin.toFloat(), yPosition)
        staticLayout.draw(canvas)
        canvas.restore()

        yPosition += staticLayout.height + 40f

        val linePaint = Paint().apply {
            color = Color.parseColor("#0D47A1")
            strokeWidth = 2f
        }
        canvas.drawLine(margin.toFloat(), yPosition, (pageWidth - margin).toFloat(), yPosition, linePaint)

        yPosition += 30f

        val footerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
            textSkewX = -0.25f
        }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateText = "Date: ${sdf.format(Date())}"
        val clinicText = "Clinic XYZ - 123 Health St. - (555) 123-4567"

        canvas.drawText(clinicText, margin.toFloat(), yPosition, footerPaint)
        yPosition += 20f
        canvas.drawText(dateText, margin.toFloat(), yPosition, footerPaint)

        document.finishPage(page)
        document.writeTo(FileOutputStream(file))
        document.close()
    }


    private fun uploadPdfToFirebase(file: File, doctorId: String, patientId: String) {
        val storageRef = storage.reference.child("prescriptions/${file.name}")
        val uri = Uri.fromFile(file)

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    savePrescriptionToFirestore(downloadUrl.toString(), doctorId, patientId)
                    sharePrescriptionLink(downloadUrl.toString())
                    clearInputs()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }


    private fun savePrescriptionToFirestore(url: String, doctorId: String, patientId: String) {
        val prescriptionData = mapOf(
            "doctorId" to doctorId,
            "patientId" to patientId,
            "url" to url,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("prescriptions")
            .add(prescriptionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Prescription saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save prescription", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sharePrescriptionLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Your Prescription")
            putExtra(Intent.EXTRA_TEXT, "Here is your prescription link:\n$link")
        }
        startActivity(Intent.createChooser(intent, "Share Prescription"))
    }

    private fun clearInputs() {
        medicationName.text?.clear()
        dosage.text?.clear()
        units.text?.clear()
        comments.text?.clear()
        patientSpinner.setSelection(0)
    }
}