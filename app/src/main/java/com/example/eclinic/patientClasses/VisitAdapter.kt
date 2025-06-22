package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import com.example.eclinic.firebase.Visit

/**
 * RecyclerView Adapter for displaying a list of visits.
 *
 * @param visits List of Visit objects to display.
 * @param onVisitClick Callback invoked when a visit item is clicked.
 */
class VisitAdapter(
    private val visits: List<Visit>,
    private val onVisitClick: (Visit) -> Unit // ✅ Click callback
) : RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {

    /**
     * ViewHolder class for visit items.
     * Holds references to the TextViews for visit name and price.
     * Handles click events on the item view.
     */
    inner class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val visitName: TextView = view.findViewById(R.id.visit_name)
        val visitPrice: TextView = view.findViewById(R.id.visit_price)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVisitClick(visits[position])
                }
            }
        }
    }

    /**
     * Creates and returns a VisitViewHolder by inflating the item layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visits, parent, false)
        return VisitViewHolder(view)
    }

    /**
     * Binds the visit data to the ViewHolder's views.
     *
     * @param holder The VisitViewHolder to bind data to.
     * @param position Position of the item in the visits list.
     */
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.visitName.text = visit.name
        holder.visitPrice.text = visit.price
    }

    /**
     * Returns the total number of visit items.
     */
    override fun getItemCount(): Int = visits.size
}
