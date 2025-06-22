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
 * RecyclerView adapter for displaying chat messages.
 * This adapter handles rendering different layouts for messages sent by the current user
 * versus messages from other users (or the bot). It also supports displaying HTML content
 * and clickable links within messages.
 *
 * @property messages The list of [ChatMessage] objects to be displayed in the chat.
 * @property currentUserId The ID of the currently logged-in user, used to determine
 * which layout to apply for each message.
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
     * ViewHolder for individual chat message items in the RecyclerView.
     * It holds the references to the UI elements of each message item.
     *
     * @param itemView The root view of the chat message item layout.
     */
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
    }

    /**
     * Returns the view type for the item at the given position.
     * This method is used to determine whether the message was sent by the current user
     * or another user, allowing the RecyclerView to use different layouts.
     *
     * @param position The position of the item in the data set.
     * @return An integer representing the view type ([VIEW_TYPE_CURRENT_USER] or [VIEW_TYPE_OTHER_USER]).
     */
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_CURRENT_USER else VIEW_TYPE_OTHER_USER
    }

    /**
     * Called when RecyclerView needs a new [ChatViewHolder] of the given type to represent an item.
     * Inflates the appropriate chat item layout based on the [viewType].
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View, as returned by [getItemViewType].
     * @return A new [ChatViewHolder] that holds a View of the given view type.
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
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [ChatViewHolder.itemView] to reflect the chat message
     * at the given position. It sets the message text, handles HTML formatting and clickable links,
     * and displays the timestamp.
     *
     * @param holder The [ChatViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        // Handle HTML formatting and clickable links
        holder.messageText.text = HtmlCompat.fromHtml(message.messageText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.messageText.movementMethod = LinkMovementMethod.getInstance()

        // Display message timestamp
        message.timestamp?.let {
            holder.timestampText.text = android.text.format.DateFormat.format("HH:mm", it).toString()
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of messages in this adapter.
     */
    override fun getItemCount(): Int = messages.size
}