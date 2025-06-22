package com.example.eclinic.patientClasses

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Prescription
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for displaying a list of prescriptions in a RecyclerView.
 *
 * @param prescriptions List of prescriptions to display.
 * @param onClick Callback invoked when a prescription item is clicked.
 */
class PrescriptionAdapter(
    private val prescriptions: List<Prescription>,
    private val onClick: (Prescription) -> Unit
) : RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    /**
     * ViewHolder representing individual prescription items.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicationName: TextView = itemView.findViewById(R.id.medicationName)
        val date: TextView = itemView.findViewById(R.id.prescriptionDate)
        val qrCodeIcon: ImageView = itemView.findViewById(R.id.qrCodeIcon)
    }

    /**
     * Inflates the item layout and creates the ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prescription, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds prescription data to the ViewHolder.
     * Fetches doctor's last name from Firestore to display with the prescription.
     * Sets click listeners for item and QR code icon.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prescription = prescriptions[position]
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("users").document(prescription.doctorId)
            .get()
            .addOnSuccessListener { doc ->
                val lastName = doc.getString("lastName") ?: "Unknown"
                holder.medicationName.text = "Prescription from Dr. $lastName"
            }
            .addOnFailureListener {
                holder.medicationName.text = "Prescription from Dr. Unknown"
            }

        holder.date.text = prescription.timestamp?.toDate()?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
        } ?: "Unknown date"
        holder.itemView.setOnClickListener {
            onClick(prescription)
        }

        holder.qrCodeIcon.setOnClickListener {
            showQrCodeDialog(holder.itemView.context, prescription)
        }
    }

    /**
     * Returns the total number of prescriptions.
     */
    override fun getItemCount(): Int = prescriptions.size
}

/**
 * Displays a dialog showing a QR code representing the prescription URL.
 *
 * @param context Context to build the dialog.
 * @param prescription The prescription containing the URL to encode.
 */
private fun showQrCodeDialog(context: Context, prescription: Prescription) {
    val qrData = prescription.url ?: return

    val barcodeEncoder = BarcodeEncoder()
    val bitmap: Bitmap = try {
        barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400)
    } catch (e: WriterException) {
        e.printStackTrace()
        return
    }

    val imageView = ImageView(context).apply {
        setImageBitmap(bitmap)
        setPadding(32, 32, 32, 32)
    }

    AlertDialog.Builder(context)
        .setTitle("Scan this QR code to open prescription")
        .setView(imageView)
        .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        .show()
}
