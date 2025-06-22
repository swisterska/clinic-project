package com.example.eclinic.doctorClasses

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * RecyclerView adapter for displaying a list of visit types and their prices for a doctor.
 * This adapter allows for editing and deleting individual visit types.
 *
 * @param items The list of visit types, where each type is represented as a Pair of (name, price).
 * @param onEditClick A lambda function to be invoked when the edit button for an item is clicked.
 * It receives the name and price of the visit type to be edited.
 * @param onDeleteClick A lambda function to be invoked when the delete button for an item is clicked.
 * It receives the name of the visit type to be deleted.
 */
class VisitTypeAdapter(
    private var items: List<Pair<String, String>>,
    private val onEditClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<VisitTypeAdapter.ViewHolder>() {

    /**
     * ViewHolder for individual visit type items in the RecyclerView.
     * Holds references to the TextViews for name and price, and ImageButtons for edit and delete actions.
     *
     * @param itemView The root view of a single list item.
     */
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvVisitName)
        val price: TextView = itemView.findViewById(R.id.tvVisitPrice)
        val editBtn: ImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [ViewHolder] that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_type, parent, false)
        return ViewHolder(view)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = items.size

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [holder]'s [itemView] to reflect the item at the given [position].
     * It also sets up click listeners for the edit and delete buttons.
     *
     * @param holder The [ViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, price) = items[position]
        holder.name.text = name
        holder.price.text = price
        holder.editBtn.setOnClickListener { onEditClick(name, price) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(name) }
    }

    /**
     * Updates the list of visit types and notifies the RecyclerView to refresh its views.
     *
     * @param newItems The new list of [Pair]s representing visit types (name to price) to be displayed.
     */
    fun updateList(newItems: List<Pair<String, String>>) {
        items = newItems
        notifyDataSetChanged() // Notifies any registered observers that the data set has changed.
    }
}