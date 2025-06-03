package com.example.snip

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in and navigate accordingly
        if (auth.currentUser == null) {
            // User is not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // User is logged in, redirect to user list
            startActivity(Intent(this, UserListActivity::class.java))
            finish()
        }
    }
}