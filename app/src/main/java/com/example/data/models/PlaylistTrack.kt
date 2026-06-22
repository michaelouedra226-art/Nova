package com.example.data.models

import androidx.room.Entity

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrack(
    val playlistId: Int,
    val trackId: String,
    val orderIndex: Int
)
