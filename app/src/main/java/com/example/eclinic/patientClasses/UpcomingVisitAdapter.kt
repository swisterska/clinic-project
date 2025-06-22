package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a single visit item with details.
 *
 * @property date Date of the visit.
 * @property hour Hour of the visit as a String.
 * @property type Type of the visit.
 * @property doctorName Name of the doctor.
 * @property documentId Firestore document ID for the visit.
 * @property price Price of the visit.
 */
data class VisitItem(
    val date: Date,
    val hour: String,
    val type: String,
    val doctorName: String,
    val documentId: String,
    val price: String
)

/**
 * Adapter for displaying a list of upcoming visits in a RecyclerView.
 *
 * @param visits List of VisitItem objects to display.
 */
class UpcomingVisitAdapter(private val visits: List<VisitItem>) :
    RecyclerView.Adapter<UpcomingVisitAdapter.VisitViewHolder>() {

    /**
     * ViewHolder class for holding the views of a single visit item.
     *
     * @param view The inflated view for the item.
     */
    class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeText: TextView = view.findViewById(R.id.textVisitType)
        val doctorNameText: TextView = view.findViewById(R.id.textDoctorName)
        val dateText: TextView = view.findViewById(R.id.textVisitDate)
        val timeText: TextView = view.findViewById(R.id.textVisitTime)
    }

    /**
     * Inflates the item view and returns a ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_visit, parent, false)
        return VisitViewHolder(view)
    }

    /**
     * Binds the visit data to the views in the ViewHolder.
     *
     * @param holder The ViewHolder containing the views.
     * @param position Position of the item in the list.
     */
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        val dateFormatter = SimpleDateFormat("EEE, d MMMM yyyy", Locale.getDefault())

        holder.typeText.text = visit.type
        holder.doctorNameText.text = visit.doctorName
        holder.dateText.text = dateFormatter.format(visit.date)
        holder.timeText.text = visit.hour
    }

    /**
     * Returns the total number of items.
     */
    override fun getItemCount() = visits.size
}
