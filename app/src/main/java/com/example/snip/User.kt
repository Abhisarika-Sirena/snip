package com.example.snip

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val timestamp: Long = System.currentTimeMillis()
) 