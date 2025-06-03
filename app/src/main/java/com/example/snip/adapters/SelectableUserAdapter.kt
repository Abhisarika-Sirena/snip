package com.example.snip.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.snip.R
import com.example.snip.models.User

class SelectableUserAdapter(
    private val onUserSelectionChanged: (User, Boolean) -> Unit
) : RecyclerView.Adapter<SelectableUserAdapter.SelectableUserViewHolder>() {

    private var users: List<User> = listOf()
    private val selectedUsers = mutableSetOf<String>()

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun getSelectedUserIds(): Set<String> = selectedUsers.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_user, parent, false)
        return SelectableUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectableUserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class SelectableUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.userName)
        private val emailText: TextView = itemView.findViewById(R.id.userEmail)
        private val checkBox: CheckBox = itemView.findViewById(R.id.userCheckbox)

        fun bind(user: User) {
            nameText.text = user.name
            emailText.text = user.email
            checkBox.isChecked = selectedUsers.contains(user.userId)

            val onClickListener = View.OnClickListener {
                val newState = !checkBox.isChecked
                checkBox.isChecked = newState
                if (newState) {
                    selectedUsers.add(user.userId)
                } else {
                    selectedUsers.remove(user.userId)
                }
                onUserSelectionChanged(user, newState)
            }

            itemView.setOnClickListener(onClickListener)
            checkBox.setOnClickListener { view ->
                val isChecked = (view as CheckBox).isChecked
                if (isChecked) {
                    selectedUsers.add(user.userId)
                } else {
                    selectedUsers.remove(user.userId)
                }
                onUserSelectionChanged(user, isChecked)
            }
        }
    }
} 