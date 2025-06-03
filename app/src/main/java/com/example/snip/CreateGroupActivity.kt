package com.example.snip

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snip.adapters.SelectableUserAdapter
import com.example.snip.models.Group
import com.example.snip.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var groupNameInput: EditText
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var createButton: Button
    private lateinit var adapter: SelectableUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        auth = Firebase.auth
        
        // Initialize views
        groupNameInput = findViewById(R.id.groupNameInput)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        createButton = findViewById(R.id.createGroupButton)

        // Setup adapter
        adapter = SelectableUserAdapter { user, isSelected ->
            // Optional: Add any logic for when user selection changes
        }

        // Setup RecyclerView
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = adapter
        
        // Load users
        loadUsers()

        // Setup create button
        createButton.setOnClickListener {
            createGroup()
        }
    }

    private fun loadUsers() {
        val currentUser = auth.currentUser ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .whereNotEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
                adapter.updateUsers(users)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading users: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createGroup() {
        val currentUser = auth.currentUser ?: return
        val groupName = groupNameInput.text.toString().trim()

        if (groupName.isEmpty()) {
            groupNameInput.error = "Group name is required"
            return
        }

        val selectedUsers = adapter.getSelectedUserIds()
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show()
            return
        }

        val members = selectedUsers.toMutableList().apply { add(currentUser.uid) }
        
        val group = Group(
            name = groupName,
            createdBy = currentUser.uid,
            createdAt = System.currentTimeMillis(),
            members = members
        )

        FirebaseFirestore.getInstance()
            .collection("groups")
            .add(group)
            .addOnSuccessListener {
                Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error creating group: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 