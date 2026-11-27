package com.example.ghichu

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class UserProfile(
    val name: String,
    val email: String,
    val createdAt: Long
)

enum class Screen { Home, List, Create, Favorites }

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date(millis))