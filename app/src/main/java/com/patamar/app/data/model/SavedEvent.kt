package com.patamar.app.data.model

import androidx.room.Entity

@Entity(tableName = "saved_events", primaryKeys = ["userId", "eventId"])
data class SavedEvent(
    val userId: String,
    val eventId: String,
    val savedAt: Long = System.currentTimeMillis()
)
