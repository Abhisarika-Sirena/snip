package com.example.snip.models

data class Group(
    val groupId: String = "",
    val name: String = "",
    val createdBy: String = "",
    val createdAt: Long = 0,
    val members: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0
) 