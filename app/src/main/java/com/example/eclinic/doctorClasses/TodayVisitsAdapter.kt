package com.example.eclinic.doctorClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.patientClasses.VisitItem

/**
 * Adapter for displaying today's visits in a RecyclerView.
 *
 * Binds VisitItem data to item views for each visit scheduled today.
 *
 * @property visits List of VisitItem objects representing today's visits.
 */
class TodayVisitsAdapter(private val visits: List<VisitItem>) :
    RecyclerView.Adapter<TodayVisitsAdapter.VisitViewHolder>() {

    /**
     * ViewHolder class for a single visit item.
     *
     * Holds references to the TextViews that display visit details.
     *
     * @param view The item view representing a visit.
     */
    class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvPatientName: TextView = view.findViewById(R.id.tvPatientName)
    }

    /**
     * Creates a new ViewHolder by inflating the visit item layout.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new VisitViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_visit, parent, false)
        return VisitViewHolder(view)
    }

    /**
     * Binds visit data to the views in the ViewHolder for a given position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the visit in the list.
     */
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.tvHour.text = visit.hour
        holder.tvType.text = visit.type
        holder.tvPatientName.text = visit.doctorName // temporarily using doctorName as patient name placeholder
    }

    /**
     * Returns the total number of visits in the list.
     *
     * @return The size of the visits list.
     */
    override fun getItemCount(): Int = visits.size
}
