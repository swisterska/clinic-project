package com.example.eclinic.calendar

import com.example.eclinic.R
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.calendar.CalendarAdapter.OnItemListener
import java.time.LocalDate


class CalendarViewHolder(
    itemView: View,
    private val onItemListener: OnItemListener,
    private val daysOfMonth: ArrayList<LocalDate?>
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val date = daysOfMonth[adapterPosition]
        onItemListener.onItemClick(adapterPosition, date)
    }
}
