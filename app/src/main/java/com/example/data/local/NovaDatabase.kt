package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.models.MediaItem
import com.example.data.models.Playlist
import com.example.data.models.PlaylistTrack
import com.example.data.models.PlaybackHistory

@Database(
    entities = [MediaItem::class, Playlist::class, PlaylistTrack::class, PlaybackHistory::class],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun novaDao(): NovaDao
}
