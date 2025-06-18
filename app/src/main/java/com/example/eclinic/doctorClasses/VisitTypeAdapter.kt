package com.example.eclinic.doctorClasses

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class VisitTypeAdapter(
    private var visits: List<Pair<String, String>>,
    private val onEditClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<VisitTypeAdapter.VisitViewHolder>() {

    inner class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvVisitName)
        val priceText: TextView = view.findViewById(R.id.tvVisitPrice)
        val editButton: ImageButton = view.findViewById(R.id.btnEdit)
        val deleteButton: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit_type, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val (name, price) = visits[position]
        holder.nameText.text = name
        holder.priceText.text = price

        holder.editButton.setOnClickListener {
            onEditClick(name, price)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(name)
        }
    }

    override fun getItemCount(): Int = visits.size

    fun updateList(newVisits: List<Pair<String, String>>) {
        visits = newVisits
        notifyDataSetChanged()
    }
}
