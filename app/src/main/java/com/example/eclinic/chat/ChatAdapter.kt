package com.example.eclinic.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CURRENT_USER = 1
        private const val VIEW_TYPE_OTHER_USER = 2
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_CURRENT_USER else VIEW_TYPE_OTHER_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_CURRENT_USER) {
            R.layout.chat_item_user
        } else {
            R.layout.chat_item_bot
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.messageText

        message.timestamp?.let {
            holder.timestampText.text = android.text.format.DateFormat.format("HH:mm", it).toString()
        }
    }

    override fun getItemCount(): Int = messages.size
}