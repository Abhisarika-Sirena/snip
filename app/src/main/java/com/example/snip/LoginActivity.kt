package com.example.snip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signupButton: MaterialButton
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Setting content view to activity_login")
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth
        
        try {
            // Initialize views
            emailInput = findViewById(R.id.emailInput)
            passwordInput = findViewById(R.id.passwordInput)
            loginButton = findViewById(R.id.loginButton)
            signupButton = findViewById(R.id.signupButton)

            // Set initial visibility
            emailInput.visibility = View.VISIBLE
            passwordInput.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE
            signupButton.visibility = View.VISIBLE

            Log.d(TAG, "Views initialized successfully")

            // Set click listeners
            loginButton.setOnClickListener { 
                Log.d(TAG, "Login button clicked")
                attemptLogin() 
            }
            signupButton.setOnClickListener { 
                Log.d(TAG, "Signup button clicked")
                startActivity(Intent(this, SignupActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun attemptLogin() {
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString()?.trim() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "Login successful")
                // Login successful
                startActivity(Intent(this, UserListActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Login failed", e)
                // Login failed
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                loginButton.isEnabled = true
                loginButton.text = getString(R.string.btn_login)
            }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        auth.currentUser?.let {
            Log.d(TAG, "User already signed in, redirecting to UserListActivity")
            // User is signed in, go to UserListActivity
            startActivity(Intent(this, UserListActivity::class.java))
            finish()
        }
    }
}
