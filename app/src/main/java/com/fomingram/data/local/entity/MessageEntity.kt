package com.fomingram.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String,
    val text: String,
    val sender: String,         // "me" | contact name
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
