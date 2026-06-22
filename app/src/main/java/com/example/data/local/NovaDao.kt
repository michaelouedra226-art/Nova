package com.example.data.local

import androidx.room.*
import com.example.data.models.MediaItem
import com.example.data.models.Playlist
import com.example.data.models.PlaylistTrack
import com.example.data.models.PlaybackHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface NovaDao {
    // --- MediaItems ---
    @Query("SELECT * FROM media_items ORDER BY title ASC")
    fun getAllMediaItems(): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: MediaItem)

    @Update
    suspend fun updateMediaItem(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaItem(id: String)

    // --- Playlists ---
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Int)

    // --- PlaylistTracks ---
    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getTracksForPlaylist(playlistId: Int): Flow<List<PlaylistTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrack)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deletePlaylistTrack(playlistId: Int, trackId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: Int)

    // --- History & Stats ---
    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT 100")
    fun getPlaybackHistoryFlow(): Flow<List<PlaybackHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(history: PlaybackHistory)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
