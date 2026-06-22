package com.example.data.repository

import com.example.data.local.NovaDao
import com.example.data.models.MediaItem
import com.example.data.models.Playlist
import com.example.data.models.PlaylistTrack
import com.example.data.models.PlaybackHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

class NovaRepository(private val novaDao: NovaDao) {

    val allMediaItems: Flow<List<MediaItem>> = novaDao.getAllMediaItems()
    val allPlaylists: Flow<List<Playlist>> = novaDao.getAllPlaylists()
    val playbackHistory: Flow<List<PlaybackHistory>> = novaDao.getPlaybackHistoryFlow()

    suspend fun checkAndSeedDatabase() {
        val currentItems = novaDao.getAllMediaItems().first()
        if (currentItems.isEmpty()) {
            val seedItems = listOf(
                MediaItem(
                    id = "music_1",
                    title = "Synthwave Eclipse",
                    artist = "Starcraft Kid",
                    album = "Nebula Drive",
                    genre = "Synthwave",
                    year = "2024",
                    durationMs = 225000L,
                    filePath = "synthwave_eclipse.mp3",
                    isVideo = false,
                    resolution = "",
                    isFavorite = true,
                    tags = "Ambiance travail, Nocturne",
                    coverPresetId = 0
                ),
                MediaItem(
                    id = "music_2",
                    title = "Golden Horizon",
                    artist = "Aether Lux",
                    album = "Chamber of Light",
                    genre = "Ambient",
                    year = "2023",
                    durationMs = 310000L,
                    filePath = "golden_horizon.mp3",
                    isVideo = false,
                    resolution = "",
                    isFavorite = false,
                    tags = "Chambre zen, Détente",
                    coverPresetId = 1
                ),
                MediaItem(
                    id = "music_3",
                    title = "Astral Drifter",
                    artist = "Cosmic Drifter",
                    album = "Void Odyssey",
                    genre = "Chillstep",
                    year = "2024",
                    durationMs = 260000L,
                    filePath = "astral_drifter.mp3",
                    isVideo = false,
                    resolution = "",
                    isFavorite = true,
                    tags = "Road trip, Ambiance travail",
                    coverPresetId = 2
                ),
                MediaItem(
                    id = "music_4",
                    title = "Cybernetic Dreams",
                    artist = "Neon Shard",
                    album = "Grid Runner",
                    genre = "Electro",
                    year = "2024",
                    durationMs = 192000L,
                    filePath = "cybernetic_dreams.mp3",
                    isVideo = false,
                    resolution = "",
                    isFavorite = false,
                    tags = "Entraînement, Code",
                    coverPresetId = 3
                ),
                MediaItem(
                    id = "music_5",
                    title = "Lofi Lunar Voyager",
                    artist = "Gravity Waves",
                    album = "Moon Dust Beats",
                    genre = "Lofi",
                    year = "2025",
                    durationMs = 164000L,
                    filePath = "lofi_lunar_voyager.mp3",
                    isVideo = false,
                    isFavorite = false,
                    tags = "Code, Détente",
                    coverPresetId = 4
                ),
                MediaItem(
                    id = "video_1",
                    title = "Cinematic Liquid Cosmos",
                    artist = "Nova Studio",
                    album = "Visual Meditations",
                    genre = "Sci-Fi",
                    year = "2025",
                    durationMs = 90000L,
                    filePath = "liquid_cosmos.mp4",
                    isVideo = true,
                    resolution = "1080p (24fps)",
                    isFavorite = false,
                    tags = "À revoir, Visuals",
                    coverPresetId = 1
                ),
                MediaItem(
                    id = "video_2",
                    title = "Futuristic Particle Storm",
                    artist = "Aether Render",
                    album = "Simulations VII",
                    genre = "Vibe loops",
                    year = "2024",
                    durationMs = 135000L,
                    filePath = "particle_storm.mp4",
                    isVideo = true,
                    resolution = "4K (60fps)",
                    isFavorite = true,
                    tags = "Visuals",
                    coverPresetId = 3
                ),
                MediaItem(
                    id = "video_3",
                    title = "Golden Aura Loop",
                    artist = "Chroma Lux",
                    album = "Ambient Visuals",
                    genre = "Relaxation",
                    year = "2025",
                    durationMs = 58000L,
                    filePath = "golden_aura.mp4",
                    isVideo = true,
                    resolution = "1080p (30fps)",
                    isFavorite = false,
                    tags = "Détente, À revoir",
                    coverPresetId = 2
                )
            )
            novaDao.insertMediaItems(seedItems)

            // Seed reference playlists
            val favoritesPlaylistId = novaDao.insertPlaylist(Playlist(name = "Mon Mix Favori"))
            val zenPlaylistId = novaDao.insertPlaylist(Playlist(name = "Ambiance Ultra-Zen"))

            // Add tracks to "Mon Mix Favori"
            novaDao.insertPlaylistTrack(PlaylistTrack(favoritesPlaylistId.toInt(), "music_1", 0))
            novaDao.insertPlaylistTrack(PlaylistTrack(favoritesPlaylistId.toInt(), "music_3", 1))
            novaDao.insertPlaylistTrack(PlaylistTrack(favoritesPlaylistId.toInt(), "video_2", 2))

            // Add tracks to "Ambiance Ultra-Zen"
            novaDao.insertPlaylistTrack(PlaylistTrack(zenPlaylistId.toInt(), "music_2", 0))
            novaDao.insertPlaylistTrack(PlaylistTrack(zenPlaylistId.toInt(), "video_3", 1))

            // Add initial history for statistics starting points
            novaDao.insertHistoryRecord(PlaybackHistory(trackId = "music_1", title = "Synthwave Eclipse", isVideo = false, durationPlayedMs = 120000, timestamp = System.currentTimeMillis() - 86400000 * 3))
            novaDao.insertHistoryRecord(PlaybackHistory(trackId = "music_2", title = "Golden Horizon", isVideo = false, durationPlayedMs = 310000, timestamp = System.currentTimeMillis() - 86400000 * 2))
            novaDao.insertHistoryRecord(PlaybackHistory(trackId = "music_3", title = "Astral Drifter", isVideo = false, durationPlayedMs = 260000, timestamp = System.currentTimeMillis() - 86400000 * 1))
            novaDao.insertHistoryRecord(PlaybackHistory(trackId = "video_2", title = "Futuristic Particle Storm", isVideo = true, durationPlayedMs = 135000, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun insertMediaItem(item: MediaItem) = novaDao.insertMediaItem(item)

    suspend fun updateMediaItem(item: MediaItem) = novaDao.updateMediaItem(item)

    suspend fun deleteMediaItem(id: String) = novaDao.deleteMediaItem(id)

    suspend fun createPlaylist(name: String) = novaDao.insertPlaylist(Playlist(name = name))

    suspend fun deletePlaylist(id: Int) = novaDao.deletePlaylist(id)

    suspend fun addTrackToPlaylist(playlistId: Int, trackId: String, orderIndex: Int) {
        novaDao.insertPlaylistTrack(PlaylistTrack(playlistId, trackId, orderIndex))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: String) {
        novaDao.deletePlaylistTrack(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Int): Flow<List<PlaylistTrack>> = novaDao.getTracksForPlaylist(playlistId)

    suspend fun recordPlayback(trackId: String, title: String, isVideo: Boolean, durationMs: Long) {
        novaDao.insertHistoryRecord(
            PlaybackHistory(
                trackId = trackId,
                title = title,
                isVideo = isVideo,
                durationPlayedMs = durationMs
            )
        )
    }

    suspend fun clearHistory() = novaDao.clearHistory()
}
