package com.example.eclinic.doctorClasses

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class VisitTypeAdapter(
    private var items: List<Pair<String, String>>,
    private val onEditClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<VisitTypeAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvVisitName)
        val price: TextView = itemView.findViewById(R.id.tvVisitPrice)
        val editBtn: ImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_type, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, price) = items[position]
        holder.name.text = name
        holder.price.text = price
        holder.editBtn.setOnClickListener { onEditClick(name, price) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(name) }
    }

    fun updateList(newItems: List<Pair<String, String>>) {
        items = newItems
        notifyDataSetChanged()
    }
}
