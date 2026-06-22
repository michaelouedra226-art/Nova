package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val year: String,
    val durationMs: Long,
    val filePath: String,
    val isVideo: Boolean,
    val resolution: String = "",
    val addedDate: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = "", // comma-separated tags
    val coverPresetId: Int = 0 // index to extract gorgeous glassmorphic cover palettes
) : Serializable
