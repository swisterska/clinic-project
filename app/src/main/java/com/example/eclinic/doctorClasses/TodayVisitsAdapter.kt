package com.example.eclinic.doctorClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.patientClasses.VisitItem

/**
 * RecyclerView adapter for displaying a list of today's scheduled visits for a doctor.
 * This adapter binds [VisitItem] data to individual view holders, showing the hour,
 * type of visit, and the patient's name.
 */
class TodayVisitsAdapter(private val visits: List<VisitItem>) :
    RecyclerView.Adapter<TodayVisitsAdapter.VisitViewHolder>() {

    /**
     * ViewHolder for individual visit items in the RecyclerView.
     * Holds references to the TextViews that display visit details.
     *
     * @param view The root view of a single list item.
     */
    class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvPatientName: TextView = view.findViewById(R.id.tvPatientName)
    }

    /**
     * Called when RecyclerView needs a new [VisitViewHolder] of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [VisitViewHolder] that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_visit, parent, false)
        return VisitViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [holder]'s [itemView] to reflect the item at the given [position].
     *
     * @param holder The [VisitViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.tvHour.text = visit.hour
        holder.tvType.text = visit.type
        // Note: Currently, tvPatientName displays visit.doctorName.
        // If this adapter is for a doctor's view of patients, it should display the patient's name.
        // Consider renaming visit.doctorName to visit.participantName or similar in VisitItem
        // if it's meant to be generic, or ensure patientName is passed for this context.
        holder.tvPatientName.text = visit.doctorName //tymczasowo
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = visits.size
}