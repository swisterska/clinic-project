package com.example.eclinic.patientClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView adapter for displaying a list of patient appointments.
 * This adapter binds [VisitItem] data to individual view holders, showing details
 * like visit type, doctor's name, date, time, and price, and provides a cancel button.
 *
 * @param appointments A mutable list of [VisitItem] objects representing the appointments to display.
 * @param onCancelClick A lambda function to be invoked when the cancel button for an appointment is clicked.
 * It receives the [VisitItem] that was clicked.
 */
class AppointmentsAdapter(
    private val appointments: MutableList<VisitItem>,
    private val onCancelClick: (VisitItem) -> Unit
) : RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    /**
     * ViewHolder for individual appointment items in the RecyclerView.
     * Holds references to the TextViews that display appointment details and the cancel button.
     *
     * @param itemView The root view of a single list item.
     */
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val visitType: TextView = itemView.findViewById(R.id.textVisitType)
        val doctorName: TextView = itemView.findViewById(R.id.textDoctorName)
        val visitDate: TextView = itemView.findViewById(R.id.textVisitDate)
        val visitTime: TextView = itemView.findViewById(R.id.textVisitTime)
        val cancelBtn: Button = itemView.findViewById(R.id.cancelAppointmentBtn)
        val visitPrice: TextView = itemView.findViewById(R.id.textVisitPrice)

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = appointments.size

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [holder]'s [itemView] to reflect the appointment
     * item at the given [position]. It also sets up the click listener for the cancel button.
     *
     * @param holder The [ViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.visitType.text = appointment.type
        holder.doctorName.text = appointment.doctorName
        // Format the date for display, e.g., "Mon, 01 Jan"
        holder.visitDate.text = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(appointment.date)
        holder.visitTime.text = appointment.hour
        holder.visitPrice.text = appointment.price
        holder.cancelBtn.visibility = if (appointment.isPast) View.GONE else View.VISIBLE



        // Set click listener for the cancel button, invoking the provided lambda
        holder.cancelBtn.setOnClickListener {
            onCancelClick(appointment)
        }
    }

    /**
     * Updates the list of appointments displayed by the adapter.
     * This method clears the existing list and adds all new appointments,
     * then notifies the RecyclerView that the data set has changed, triggering a re-render.
     *
     * @param newAppointments The new list of [VisitItem] objects to be displayed.
     */
    fun updateAppointments(newAppointments: List<VisitItem>) {
        appointments.clear() // Clear existing items
        appointments.addAll(newAppointments) // Add all new items
        notifyDataSetChanged() // Notify adapter that data has changed
    }
}