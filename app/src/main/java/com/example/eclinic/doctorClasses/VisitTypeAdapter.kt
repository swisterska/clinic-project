package com.example.eclinic.doctorClasses

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * Adapter for displaying a list of visit types in a RecyclerView.
 *
 * Each item shows the visit name and price, along with edit and delete buttons.
 *
 * @param items List of pairs where first is visit name and second is visit price.
 * @param onEditClick Lambda function called when the edit button is clicked, with visit name and price as parameters.
 * @param onDeleteClick Lambda function called when the delete button is clicked, with visit name as parameter.
 */
class VisitTypeAdapter(
    private var items: List<Pair<String, String>>,
    private val onEditClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<VisitTypeAdapter.ViewHolder>() {

    /**
     * ViewHolder for the visit type item view.
     *
     * Holds references to the TextViews and buttons for name, price, edit, and delete.
     *
     * @param itemView The view of a single visit type item.
     */
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvVisitName)
        val price: TextView = itemView.findViewById(R.id.tvVisitPrice)
        val editBtn: ImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    /**
     * Inflates the item layout and creates the ViewHolder.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_type, parent, false)
        return ViewHolder(view)
    }

    /**
     * Returns the total number of visit type items.
     *
     * @return Number of items in the list.
     */
    override fun getItemCount() = items.size

    /**
     * Binds data for the visit type at the given position.
     *
     * Sets the visit name and price, and configures click listeners for edit and delete buttons.
     *
     * @param holder The ViewHolder holding the views.
     * @param position The position in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, price) = items[position]
        holder.name.text = name
        holder.price.text = price
        holder.editBtn.setOnClickListener { onEditClick(name, price) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(name) }
    }

    /**
     * Updates the adapter's data set and refreshes the RecyclerView.
     *
     * @param newItems New list of visit types.
     */
    fun updateList(newItems: List<Pair<String, String>>) {
        items = newItems
        notifyDataSetChanged()
    }
}
