package com.example.snip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snip.adapters.GroupAdapter
import com.example.snip.models.Group
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class GroupsActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val TAG = "GroupsActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var adapter: GroupAdapter
    private lateinit var emptyView: View
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        auth = Firebase.auth
        
        // Initialize views
        recyclerView = findViewById(R.id.groupsRecyclerView)
        bottomNav = findViewById(R.id.bottomNavigation)
        fab = findViewById(R.id.fabCreateGroup)
        emptyView = findViewById(R.id.emptyView)
        progressBar = findViewById(R.id.progressBar)

        // Setup adapter
        adapter = GroupAdapter { group ->
            // Start chat activity for the selected group
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("chatId", group.groupId)
                putExtra("chatName", group.name)
                putExtra("isGroup", true)
            }
            startActivity(intent)
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Setup bottom navigation
        bottomNav.setOnNavigationItemSelectedListener(this)
        bottomNav.selectedItemId = R.id.navigation_groups

        // Setup FAB
        fab.setOnClickListener {
            startActivity(Intent(this, CreateGroupActivity::class.java))
        }

        // Load groups
        loadGroups()
    }

    override fun onResume() {
        super.onResume()
        loadGroups() // Reload groups when returning to this screen
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun showEmpty(show: Boolean) {
        emptyView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        progressBar.visibility = View.GONE
    }

    private fun loadGroups() {
        val currentUser = auth.currentUser ?: return
        Log.d(TAG, "Loading groups for user: ${currentUser.uid}")
        
        showLoading(true)

        FirebaseFirestore.getInstance()
            .collection("groups")
            .whereArrayContains("members", currentUser.uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error loading groups", e)
                    showLoading(false)
                    if (e.message?.contains("requires an index") == true) {
                        Toast.makeText(this, 
                            "Setting up database... Please wait a few minutes and try again.", 
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error loading groups: ${e.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Group::class.java)?.copy(groupId = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Group", e)
                        null
                    }
                } ?: listOf()

                Log.d(TAG, "Loaded ${groups.size} groups")
                adapter.updateGroups(groups)
                
                showLoading(false)
                showEmpty(groups.isEmpty())
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_chats -> {
                startActivity(Intent(this, UserListActivity::class.java))
                finish()
                return true
            }
            R.id.navigation_groups -> return true
        }
        return false
    }
} 