package com.tellmewhy.domain.model // Or your preferred package for database-related classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "justification_log")
data class JustificationEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated primary key
    val appName: String,
    val packageName: String,
    val justificationText: String,
    val timestamp: Long // Store timestamp as Long (milliseconds since epoch)
)