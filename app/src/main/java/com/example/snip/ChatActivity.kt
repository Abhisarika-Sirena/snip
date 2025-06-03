package com.example.snip

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ChatActivity"
    }

    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var chatAdapter: ChatAdapter
    private var otherUserId: String? = null
    private var currentRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        // Get other user's ID from intent
        otherUserId = intent.getStringExtra("otherUserId")
        Log.d(TAG, "Other user ID: $otherUserId")

        if (otherUserId == null || auth.currentUser == null) {
            Log.e(TAG, "Invalid chat session - otherUserId: $otherUserId, currentUser: ${auth.currentUser?.uid}")
            Toast.makeText(this, "Error: Invalid chat session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)
        toolbar = findViewById(R.id.toolbar)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Load recipient's name
        loadRecipientName()

        // Setup RecyclerView
        chatAdapter = ChatAdapter(auth.currentUser!!.uid)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        // Create or get chat room ID
        setupChatRoom()

        // Setup send button
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }
    }

    private fun loadRecipientName() {
        otherUserId?.let { userId ->
            Firebase.firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "Chat"
                    Log.d(TAG, "Loaded recipient name: $name")
                    toolbar.title = name
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading recipient name", e)
                    toolbar.title = "Chat"
                }
        }
    }

    private fun setupChatRoom() {
        val currentUserId = auth.currentUser!!.uid
        currentRoomId = if (currentUserId < otherUserId!!) {
            "${currentUserId}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserId}"
        }
        Log.d(TAG, "Chat room ID: $currentRoomId")

        // Listen for messages
        database.child("chats").child(currentRoomId!!).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let { 
                    Log.d(TAG, "New message received: ${it.text}")
                    chatAdapter.addMessage(it)
                    recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle message updates if needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle message deletion if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle message reordering if needed
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error", error.toException())
                Toast.makeText(this@ChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage(text: String) {
        val currentUserId = auth.currentUser!!.uid
        otherUserId?.let { receiverId ->
            val message = Message(
                senderId = currentUserId,
                receiverId = receiverId,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            Log.d(TAG, "Sending message: $text")
            currentRoomId?.let { roomId ->
                database.child("chats").child(roomId)
                    .push()
                    .setValue(message)
                    .addOnSuccessListener {
                        Log.d(TAG, "Message sent successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to send message", e)
                        Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
