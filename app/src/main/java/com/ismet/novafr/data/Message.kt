package com.ismet.novafr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Sender {
    USER,
    AI
}

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
)
