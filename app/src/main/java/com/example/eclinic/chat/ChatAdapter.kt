package com.example.eclinic.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.text.method.LinkMovementMethod
import androidx.core.text.HtmlCompat
import com.example.eclinic.R

/**
 * RecyclerView adapter for displaying chat messages between users.
 *
 * Supports different layouts for messages sent by the current user and others.
 * Messages can contain HTML-formatted text and clickable links.
 *
 * @param messages List of chat messages to display.
 * @param currentUserId The ID of the current user, used to differentiate message layouts.
 */
class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CURRENT_USER = 1
        private const val VIEW_TYPE_OTHER_USER = 2
    }

    /**
     * ViewHolder class representing a single chat message item.
     *
     * @param itemView The root view of the chat message layout.
     */
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
    }

    /**
     * Determines the type of view for the message at the given position.
     *
     * Returns a different view type for messages sent by the current user vs. others.
     *
     * @param position Position of the message in the list.
     * @return An integer representing the view type.
     */
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_CURRENT_USER else VIEW_TYPE_OTHER_USER
    }

    /**
     * Inflates the appropriate chat message layout based on the view type.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The type of the new view.
     * @return A new ChatViewHolder containing the inflated view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_CURRENT_USER) {
            R.layout.chat_item_user
        } else {
            R.layout.chat_item_bot
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    /**
     * Binds the chat message data to the views in the ViewHolder.
     *
     * Sets the message text with HTML formatting and makes links clickable.
     * Also formats and displays the message timestamp.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the message in the list.
     */
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        holder.messageText.text = HtmlCompat.fromHtml(message.messageText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.messageText.movementMethod = LinkMovementMethod.getInstance()

        message.timestamp?.let {
            holder.timestampText.text = android.text.format.DateFormat.format("HH:mm", it).toString()
        }
    }

    /**
     * Returns the total number of messages in the chat.
     *
     * @return Number of messages.
     */
    override fun getItemCount(): Int = messages.size
}
