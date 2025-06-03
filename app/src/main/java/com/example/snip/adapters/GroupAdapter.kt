package com.example.snip.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.snip.R
import com.example.snip.models.Group
import java.text.SimpleDateFormat
import java.util.*

class GroupAdapter(
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private var groups: List<Group> = listOf()
    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    fun updateGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount() = groups.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.groupNameText)
        private val lastMessageText: TextView = itemView.findViewById(R.id.lastMessageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(group: Group) {
            nameText.text = group.name
            lastMessageText.text = group.lastMessage.takeIf { it.isNotEmpty() } ?: "No messages yet"
            timeText.text = if (group.lastMessageTime > 0) {
                dateFormat.format(Date(group.lastMessageTime))
            } else {
                dateFormat.format(Date(group.createdAt))
            }

            itemView.setOnClickListener {
                onGroupClick(group)
            }
        }
    }
} 