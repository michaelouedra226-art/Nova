package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: String,
    val title: String,
    val isVideo: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val durationPlayedMs: Long
)
