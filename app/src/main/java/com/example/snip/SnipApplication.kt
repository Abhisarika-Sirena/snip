package com.example.snip

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SnipApplication : Application() {
    companion object {
        private const val TAG = "SnipApplication"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            // Initialize Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized successfully with app: ${app?.name}")
                
                // Enable Firestore offline persistence
                val db = Firebase.firestore
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
                
                // Initialize Auth
                val auth = Firebase.auth
                auth.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    Log.d(TAG, "Auth state changed. User: ${user?.uid ?: "null"}")
                }
                
                Log.d(TAG, "Firebase services initialized successfully")
            } else {
                Log.d(TAG, "Firebase was already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
            e.printStackTrace()
        }
    }
} 