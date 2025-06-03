package com.example.snip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserListActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val TAG = "UserListActivity"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        setContentView(R.layout.activity_user_list)

        auth = FirebaseAuth.getInstance()
        
        if (auth.currentUser == null) {
            Log.d(TAG, "No user logged in, redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        verifyCurrentUser()

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Setup adapter
        adapter = UserAdapter(users) { user ->
            startChat(user)
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup bottom navigation
        bottomNav.setOnNavigationItemSelectedListener(this)
        bottomNav.selectedItemId = R.id.navigation_chats

        // Load users
        loadUsers()
    }

    private fun verifyCurrentUser() {
        val currentUserId = auth.currentUser?.uid
        Log.d(TAG, "Verifying current user: $currentUserId")

        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null!")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Current user is null!")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        Firebase.firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Current user exists in Firestore: ${document.data}")
                    loadUsers()
                } else {
                    Log.d(TAG, "Creating new user profile in Firestore")
                    // Create user profile if it doesn't exist
                    val user = User(
                        userId = currentUserId,
                        name = currentUser.displayName ?: currentUser.email?.substringBefore('@') ?: "User",
                        email = currentUser.email ?: "",
                        timestamp = System.currentTimeMillis()
                    )

                    // First create the user document
                    Firebase.firestore.collection("users").document(currentUserId)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully created user profile")
                            // Verify the document was created by reading it back
                            Firebase.firestore.collection("users").document(currentUserId)
                                .get()
                                .addOnSuccessListener { verifyDoc ->
                                    if (verifyDoc.exists()) {
                                        Log.d(TAG, "Verified user profile creation")
                                        loadUsers()
                                    } else {
                                        Log.e(TAG, "Failed to verify user profile creation")
                                        Toast.makeText(this, "Error creating profile. Please try again.", Toast.LENGTH_SHORT).show()
                                        auth.signOut()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error verifying user profile", e)
                                    Toast.makeText(this, "Error verifying profile: ${e.message}", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error creating user profile", e)
                            Toast.makeText(this, "Error creating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error verifying current user", e)
                Toast.makeText(this, "Error verifying user: ${e.message}", Toast.LENGTH_SHORT).show()
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        auth.signOut()
        // Start LoginActivity and clear the back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(users) { user ->
            startChat(user)
        }
        recyclerView.adapter = adapter
        Log.d(TAG, "RecyclerView setup completed")
    }

    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid
        Log.d(TAG, "Loading users, current user ID: $currentUserId")

        // Query users collection, ordered by most recently joined
        Firebase.firestore.collection("users")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Firestore query returned ${documents.size()} documents")
                users.clear()
                
                for (document in documents) {
                    try {
                        val userData = document.data
                        Log.d(TAG, "Processing user document: $userData")
                        
                        val user = User(
                            userId = userData["userId"] as? String ?: "",
                            name = userData["name"] as? String ?: "Unknown",
                            email = userData["email"] as? String ?: "",
                            timestamp = userData["timestamp"] as? Long ?: System.currentTimeMillis()
                        )
                        
                        if (user.userId.isNotEmpty() && user.userId != currentUserId) {
                            Log.d(TAG, "Adding user to list: ${user.name} (${user.userId})")
                            users.add(user)
                        } else {
                            Log.d(TAG, "Skipping user: ${user.name} (${user.userId})")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to User: ${document.data}", e)
                    }
                }

                Log.d(TAG, "Final users list size: ${users.size}")
                if (users.isEmpty()) {
                    Log.d(TAG, "No other users found")
                    Toast.makeText(this, "No other users found", Toast.LENGTH_SHORT).show()
                }
                
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading users", e)
                Toast.makeText(this, "Error loading users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startChat(user: User) {
        Log.d(TAG, "Starting chat with user: ${user.name} (${user.userId})")
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otherUserId", user.userId)
            putExtra("otherUserName", user.name)
            putExtra("otherUserEmail", user.email)
        }
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_chats -> return true
            R.id.navigation_groups -> {
                startActivity(Intent(this, GroupsActivity::class.java))
                finish()
                return true
            }
        }
        return false
    }
}

class UserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.userName)
        val emailText: TextView = itemView.findViewById(R.id.userEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.nameText.text = user.name
        holder.emailText.text = user.email
        holder.itemView.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = users.size
} 