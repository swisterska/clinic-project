//package com.example.eclinic.calendar
//
//import android.os.Build
//import com.example.eclinic.R
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.annotation.RequiresApi
//import androidx.recyclerview.widget.RecyclerView
//import java.time.LocalDate
//
//
//class CalendarAdapter(
//    private val daysOfMonth: ArrayList<LocalDate?>,
//    private val onItemListener: OnItemListener,
//    private val selectedDate: LocalDate?
//) : RecyclerView.Adapter<CalendarViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val view: View = inflater.inflate(R.layout.calendar_cell, parent, false)
//        val layoutParams = view.layoutParams
//        layoutParams.height = (parent.height * 0.166666666).toInt()
//        return CalendarViewHolder(view, onItemListener, daysOfMonth)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
//        val date = daysOfMonth[position]
//        holder.dayOfMonth.text = date?.dayOfMonth?.toString() ?: ""
//
//        //highlights the selected day
//        if (date != null && date == selectedDate) {
//            holder.dayOfMonth.setBackgroundResource(R.drawable.selected_day_background)
//        } else {
//            holder.itemView.setBackgroundResource(0)
//        }
//
//    }
//
//    override fun getItemCount(): Int {
//        return daysOfMonth.size
//    }
//
//    interface OnItemListener {
//        fun onItemClick(position: Int, date: LocalDate?)
//    }
//
//
//}
