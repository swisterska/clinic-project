package com.example.eclinic.doctorClasses

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
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
import com.example.eclinic.chat.ChatMessage
import com.example.eclinic.chat.ChatUtils
import com.google.firebase.firestore.FieldValue


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

        val patientNames = mutableListOf<String>()

        db.collection("confirmedAppointments")
            .whereEqualTo("doctorId", doctorId)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val patientId = doc.getString("id") ?: continue

                    db.collection("users").document(patientId).get()
                        .addOnSuccessListener { userDoc ->
                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""
                            val fullName = "$firstName $lastName".trim()

                            if (!patientMap.containsKey(fullName)) {
                                patientNames.add(fullName)
                                patientMap[fullName] = patientId
                            }

                            val adapter = ArrayAdapter(
                                this,
                                android.R.layout.simple_spinner_item,
                                patientNames
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            patientSpinner.adapter = adapter
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to load patients", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
    }

    private fun getDoctorName(onResult: (String) -> Unit) {
        val doctorId = auth.currentUser?.uid ?: return

        db.collection("users").document(doctorId)
            .get()
            .addOnSuccessListener { doc ->
                val firstName = doc.getString("firstName") ?: ""
                val lastName = doc.getString("lastName") ?: ""
                Log.d("PrescriptionsDoc", "Doctor found: $firstName $lastName")
                onResult("$firstName $lastName")
            }
            .addOnFailureListener {
                onResult("Unknown Doctor")
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

        getDoctorName { doctorName ->
            try {
                generateStyledPdf(
                    file = file,
                    patientName = selectedPatientName,
                    medication = medication,
                    dosage = dose,
                    units = units,
                    comments = comment,
                    doctorName = doctorName
                )
                uploadPdfToFirebase(file, doctorId, patientId)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun generateStyledPdf(
        file: File,
        patientName: String,
        medication: String,
        dosage: String,
        units: String,
        comments: String,
        doctorName: String
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

        canvas.drawText(dateText, margin.toFloat(), yPosition, footerPaint)
        yPosition += 20f
        canvas.drawText("Prescribed by: Dr. $doctorName", margin.toFloat(), yPosition, footerPaint)


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
            .document(patientId)
            .collection("prescriptionsList")
            .add(prescriptionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Prescription saved", Toast.LENGTH_SHORT).show()
                val hyperlink = "<a href=\"$url\">here</a>"
                val messageText = "A new prescription was issued for you. You can check it $hyperlink or in the prescription tab in the app."
                ChatUtils.sendMessage(
                    fromId = doctorId,
                    toId = patientId,
                    messageText = messageText
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save prescription", Toast.LENGTH_SHORT).show()
            }
    }


    private fun clearInputs() {
        medicationName.text?.clear()
        dosage.text?.clear()
        units.text?.clear()
        comments.text?.clear()
        patientSpinner.setSelection(0)
    }
//companion object {
//    fun sendChatMessageToPatient(patientId: String, messageText: String) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val firestore = FirebaseFirestore.getInstance()
//
//        val participantsSorted = listOf(currentUserId, patientId).sorted()
//        val generatedChatId = "${participantsSorted[0]}_${participantsSorted[1]}"
//
//        val chatRef = firestore.collection("chats").document(generatedChatId)
//
//        chatRef.get().addOnSuccessListener { docSnapshot ->
//            if (!docSnapshot.exists()) {
//                val newChatData = hashMapOf(
//                    "participants" to participantsSorted,
//                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
//                    "lastMessageText" to messageText
//                )
//                chatRef.set(newChatData)
//            } else {
//                chatRef.update(
//                    "lastMessageTimestamp", FieldValue.serverTimestamp(),
//                    "lastMessageText", messageText
//                )
//            }
//
//            val message = ChatMessage(
//                senderId = currentUserId,
//                receiverId = patientId,
//                messageText = messageText
//            )
//
//            chatRef.collection("messages")
//                .add(message)
//                .addOnSuccessListener {
//                    Log.d("PrescriptionsDoc", "Wysłano wiadomość o recepcie do pacjenta.")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("PrescriptionsDoc", "Błąd wysyłania wiadomości: ${e.message}")
//                }
//        }.addOnFailureListener { e ->
//            Log.e("PrescriptionsDoc", "Błąd sprawdzania czatu: ${e.message}")
//        }
//    }
//}

}