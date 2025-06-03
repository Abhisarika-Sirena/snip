package com.example.snip

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SignupActivity"
    }

    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var signupButton: MaterialButton
    private lateinit var loginButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView
    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore by lazy { Firebase.firestore }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = Firebase.auth
        
        // Initialize views
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        signupButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)
        nameLayout = findViewById(R.id.nameLayout)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)

        // Set click listeners
        signupButton.setOnClickListener { attemptSignup() }
        loginButton.setOnClickListener { 
            finish() // Go back to LoginActivity
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        loadingText.visibility = if (show) View.VISIBLE else View.GONE
        signupButton.isEnabled = !show
        loginButton.isEnabled = !show
        nameInput.isEnabled = !show
        emailInput.isEnabled = !show
        passwordInput.isEnabled = !show
        confirmPasswordInput.isEnabled = !show
    }

    private fun attemptSignup() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show()
            return
        }

        // Reset any previous errors
        nameLayout.error = null
        emailLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        val name = nameInput.text?.toString()?.trim() ?: ""
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString()?.trim() ?: ""
        val confirmPassword = confirmPasswordInput.text?.toString()?.trim() ?: ""

        // Validate inputs
        var hasError = false
        
        if (name.isEmpty()) {
            nameLayout.error = "Name is required"
            hasError = true
        }
        
        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            hasError = true
        }
        
        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            hasError = true
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            hasError = true
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Please confirm password"
            hasError = true
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            return
        }

        // Show loading state
        showLoading(true)

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId == null) {
                    showLoading(false)
                    Toast.makeText(this, "Error: Failed to get user ID", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                
                // Create User object
                val user = User(
                    userId = userId,
                    name = name,
                    email = email,
                    timestamp = System.currentTimeMillis()
                )

                // Save user to Firestore and proceed
                db.collection("users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        Log.d(TAG, "User profile created successfully")
                        // Proceed to UserListActivity immediately
                        val intent = Intent(this, UserListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        Log.e(TAG, "Error saving user data to Firestore", e)
                        Toast.makeText(this, "Error creating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        // Don't sign out - let them try again or proceed anyway
                    }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e(TAG, "createUserWithEmail:failure", e)
                handleFirebaseError(e)
            }
    }

    private fun handleFirebaseError(e: Exception) {
        val errorMessage = when (e) {
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak"
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email format"
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Email already in use"
            else -> "Registration failed: ${e.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}
