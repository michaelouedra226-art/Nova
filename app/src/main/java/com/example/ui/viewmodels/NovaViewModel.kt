package com.example.ui.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.NovaDatabase
import com.example.data.models.MediaItem
import com.example.data.models.Playlist
import com.example.data.models.PlaylistTrack
import com.example.data.models.PlaybackHistory
import com.example.data.repository.NovaRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.sin

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val database: NovaDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            NovaDatabase::class.java,
            "nova_player_db"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository: NovaRepository by lazy {
        NovaRepository(database.novaDao())
    }

    // --- Core Media Flows ---
    val allItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playbackHistory = MutableStateFlow<List<PlaybackHistory>>(emptyList())

    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedGenreFilter = MutableStateFlow<String?>(null)
    val selectedMediaTypeFilter = MutableStateFlow<String?>("AUDIO") // "AUDIO", "VIDEO", "ALL"

    // --- Live Playing Queue & Playback States ---
    val currentPlayingQueue = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentPlayingIndex = MutableStateFlow(-1)
    val isPlaying = MutableStateFlow(false)
    val playbackProgressMs = MutableStateFlow(0L)
    val playbackSpeed = MutableStateFlow(1.0f)

    // --- Music Customizations ---
    val equalizerPreset = MutableStateFlow("Flat") // "Pop", "Rock", "Bass Boost", "Vocal", "Flat", "Personnalisé"
    val equalizerBands = MutableStateFlow(listOf(50, 50, 50, 50, 50)) // 5 bands in 1-100 range
    val crossfadeDurationSec = MutableStateFlow(3) // Transition crossfade duration in sec
    val isShuffleEnabled = MutableStateFlow(false)
    val isRepeatOneEnabled = MutableStateFlow(false) // False: repeat list index, True: repeat single item

    // --- Sleep Timer & Alarms ---
    val sleepTimerMinutesLeft = MutableStateFlow<Int?>(null)
    val isSleepTimerWarningActive = MutableStateFlow(false)
    val isWakeUpAlarmEnabled = MutableStateFlow(false)
    val wakeUpAlarmTimeString = MutableStateFlow("07:30")

    // --- Video Player States ---
    val videoPlayPositionMap = MutableStateFlow<Map<String, Long>>(emptyMap()) // trackId -> lastMs
    val currentVideoSubtitleSize = MutableStateFlow(16) // sp
    val currentVideoSubtitleColor = MutableStateFlow("Gold") // "White", "Gold", "Cyan"
    val currentVideoSubtitleDelayMs = MutableStateFlow(0L) // shift delay
    val isVideoLocked = MutableStateFlow(false)
    val continueWatchingList = MutableStateFlow<List<MediaItem>>(emptyList())
    val capturedScreenPreview = MutableStateFlow<String?>(null) // Simulation of capturing videos

    // Brightness and Volume Swipe Overlays (In-App)
    val swipeBrightnessVal = MutableStateFlow(60) // 1 to 100
    val swipeVolumeVal = MutableStateFlow(50) // 1 to 100

    // Custom In-App Picture-in-Picture Floating Window
    val isFloatingVideoActive = MutableStateFlow(false)
    val floatingVideoOffsetX = MutableStateFlow(30f)
    val floatingVideoOffsetY = MutableStateFlow(250f)

    // --- Statistics States ---
    val totalListeningMinutes = MutableStateFlow(0)
    val favoriteGenrePercentages = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val statsDailyMinutes = MutableStateFlow(listOf(15, 32, 45, 12, 60, 24, 48)) // last 7 days

    // General Customization Options
    val isDarkMode = MutableStateFlow(true)
    val isCompactMode = MutableStateFlow(false)
    val excludedFolders = MutableStateFlow(listOf("/system/notifications", "/whatsapp/audios"))

    // Current details for player
    val currentPlayingItem: StateFlow<MediaItem?> = combine(currentPlayingQueue, currentPlayingIndex) { queue, index ->
        if (queue.isNotEmpty() && index in queue.indices) queue[index] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- High Fidelity Atmospheric Sci-Fi Audio Synthesizer Engine ---
    private var synthJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var playbackProgressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Init database seeding and collection
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            
            // Gather items
            repository.allMediaItems.collect { itemsList ->
                allItems.value = itemsList
                updateStatistics(itemsList)
            }
        }

        viewModelScope.launch {
            repository.allPlaylists.collect { playlistsList ->
                playlists.value = playlistsList
            }
        }

        viewModelScope.launch {
            repository.playbackHistory.collect { historyList ->
                playbackHistory.value = historyList
            }
        }

        // Periodically verify waking alarms (mock check)
        viewModelScope.launch {
            while (isActive) {
                delay(15000)
                if (isWakeUpAlarmEnabled.value) {
                    // Simulates wake up trigger
                }
            }
        }
    }

    // --- Core Actions ---
    fun togglePlayPause() {
        val playing = !isPlaying.value
        isPlaying.value = playing
        if (playing) {
            startSynthesizer()
            startTrackProgressTracker()
        } else {
            stopSynthesizer()
            stopTrackProgressTracker()
        }
    }

    fun playTrackNow(track: MediaItem) {
        val currentQueue = currentPlayingQueue.value
        val index = currentQueue.indexOfFirst { it.id == track.id }
        if (index != -1) {
            currentPlayingIndex.value = index
        } else {
            // Append and play
            val newQueue = currentQueue.toMutableList().apply { add(track) }
            currentPlayingQueue.value = newQueue
            currentPlayingIndex.value = newQueue.size - 1
        }
        isPlaying.value = true
        playbackProgressMs.value = 0L
        startSynthesizer()
        startTrackProgressTracker()

        // Record history
        viewModelScope.launch {
            repository.recordPlayback(track.id, track.title, track.isVideo, 45000)
            updateStatistics(allItems.value)
        }
    }

    fun setQueueAndPlay(queue: List<MediaItem>, startIndex: Int) {
        currentPlayingQueue.value = queue
        currentPlayingIndex.value = startIndex
        isPlaying.value = true
        playbackProgressMs.value = 0L
        startSynthesizer()
        startTrackProgressTracker()

        val track = queue.getOrNull(startIndex)
        if (track != null) {
            viewModelScope.launch {
                repository.recordPlayback(track.id, track.title, track.isVideo, 45000)
                updateStatistics(allItems.value)
            }
        }
    }

    fun playNext() {
        val queue = currentPlayingQueue.value
        if (queue.isEmpty()) return
        
        var nextIndex = currentPlayingIndex.value + 1
        if (isShuffleEnabled.value) {
            nextIndex = queue.indices.random()
        } else if (nextIndex >= queue.size) {
            nextIndex = 0
        }
        
        currentPlayingIndex.value = nextIndex
        playbackProgressMs.value = 0L
        val track = queue.getOrNull(nextIndex)
        if (track != null) {
            viewModelScope.launch {
                repository.recordPlayback(track.id, track.title, track.isVideo, 45000)
            }
        }
    }

    fun playPrevious() {
        val queue = currentPlayingQueue.value
        if (queue.isEmpty()) return
        
        var prevIndex = currentPlayingIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = queue.size - 1
        }
        currentPlayingIndex.value = prevIndex
        playbackProgressMs.value = 0L
    }

    fun toggleLikeTrack(trackId: String) {
        viewModelScope.launch {
            val list = allItems.value
            val match = list.find { it.id == trackId }
            if (match != null) {
                val updated = match.copy(isFavorite = !match.isFavorite)
                repository.updateMediaItem(updated)
            }
        }
    }

    fun seekPlaybackTo(positionMs: Long) {
        playbackProgressMs.value = positionMs
    }

    // --- Queue Modifiers ---
    fun addToQueue(item: MediaItem) {
        val curr = currentPlayingQueue.value.toMutableList()
        if (curr.none { it.id == item.id }) {
            curr.add(item)
            currentPlayingQueue.value = curr
        }
    }

    fun removeFromQueue(trackId: String) {
        val curr = currentPlayingQueue.value.toMutableList()
        val index = curr.indexOfFirst { it.id == trackId }
        if (index != -1) {
            curr.removeAt(index)
            currentPlayingQueue.value = curr
            // Adjust index if needed
            val currentIdx = currentPlayingIndex.value
            if (currentIdx >= curr.size) {
                currentPlayingIndex.value = curr.size - 1
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val curr = currentPlayingQueue.value.toMutableList()
        if (fromIndex in curr.indices && toIndex in curr.indices) {
            val temp = curr.removeAt(fromIndex)
            curr.add(toIndex, temp)
            currentPlayingQueue.value = curr
            if (currentPlayingIndex.value == fromIndex) {
                currentPlayingIndex.value = toIndex
            }
        }
    }

    // --- Playlist Customizations ---
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Int, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId, 0)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Int, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun updateMediaMetadata(trackId: String, title: String, artist: String, album: String, genre: String) {
        viewModelScope.launch {
            val match = allItems.value.find { it.id == trackId }
            if (match != null) {
                val updated = match.copy(title = title, artist = artist, album = album, genre = genre)
                repository.updateMediaItem(updated)
            }
        }
    }

    // --- Equalizer Presets ---
    fun selectEqualizerPreset(preset: String) {
        equalizerPreset.value = preset
        when (preset) {
            "Pop" -> equalizerBands.value = listOf(35, 55, 75, 50, 40)
            "Rock" -> equalizerBands.value = listOf(68, 48, 35, 55, 75)
            "Bass Boost" -> equalizerBands.value = listOf(95, 80, 50, 40, 30)
            "Vocal" -> equalizerBands.value = listOf(25, 45, 85, 80, 55)
            "Flat" -> equalizerBands.value = listOf(50, 50, 50, 50, 50)
        }
    }

    fun updateSingleEqualizerBand(index: Int, rawValue: Int) {
        equalizerPreset.value = "Personnalisé"
        val curr = equalizerBands.value.toMutableList()
        if (index in curr.indices) {
            curr[index] = rawValue
            equalizerBands.value = curr
        }
    }

    // --- Sleep Timer Logic ---
    fun startSleepTimer(minutes: Int) {
        sleepTimerMinutesLeft.value = minutes
        isSleepTimerWarningActive.value = false
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            var left = minutes
            while (left > 0) {
                delay(60000)
                left--
                sleepTimerMinutesLeft.value = left
                if (left == 1) {
                    // Activate sleep cutoff warning HUD
                    isSleepTimerWarningActive.value = true
                }
            }
            // Cutoff sound with dynamic fade out
            fadeOutAndPause()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerMinutesLeft.value = null
        isSleepTimerWarningActive.value = false
    }

    private suspend fun fadeOutAndPause() {
        // Dynamic speed fade
        var speedMod = 1.0f
        while (speedMod > 0.0f) {
            speedMod -= 0.15f
            playbackSpeed.value = speedMod.coerceAtLeast(0.1f)
            delay(150)
        }
        isPlaying.value = false
        stopSynthesizer()
        stopTrackProgressTracker()
        playbackSpeed.value = 1.0f // reset
        cancelSleepTimer()
    }

    // --- Video Specific Handlers ---
    fun saveVideoLastPosition(id: String, positionMs: Long) {
        val currMap = videoPlayPositionMap.value.toMutableMap()
        currMap[id] = positionMs
        videoPlayPositionMap.value = currMap

        // Update list of continuing videos
        viewModelScope.launch {
            val unwatched = allItems.value.filter {
                it.isVideo && (videoPlayPositionMap.value[it.id] ?: 0L) > 0L &&
                (videoPlayPositionMap.value[it.id] ?: 0L) < it.durationMs - 5000
            }
            continueWatchingList.value = unwatched
        }
    }

    fun captureVideoScreenshotSimulated() {
        // Generates screenshot preview notification trigger
        capturedScreenPreview.value = "nova_frame_${System.currentTimeMillis()}"
        viewModelScope.launch {
            delay(4000)
            capturedScreenPreview.value = null
        }
    }

    // --- Synthesizer Track Progress ---
    private fun startTrackProgressTracker() {
        playbackProgressJob?.cancel()
        playbackProgressJob = viewModelScope.launch {
            while (isActive) {
                delay(100)
                val current = currentPlayingItem.value
                if (isPlaying.value && current != null) {
                    val nextProgress = playbackProgressMs.value + (100 * playbackSpeed.value).toLong()
                    if (nextProgress >= current.durationMs) {
                        playbackProgressMs.value = 0L
                        playNext()
                    } else {
                        playbackProgressMs.value = nextProgress
                    }
                }
            }
        }
    }

    private fun stopTrackProgressTracker() {
        playbackProgressJob?.cancel()
    }

    // --- Statistics Accumulator ---
    private fun updateStatistics(items: List<MediaItem>) {
        viewModelScope.launch {
            val totalMinutesResult = 120 + (statsDailyMinutes.value.sum())
            totalListeningMinutes.value = totalMinutesResult

            val genresCount = items.groupBy { it.genre }.mapValues { it.value.size }
            val totalCount = items.size.toFloat()
            if (totalCount > 0) {
                val percentages = genresCount.map {
                    it.key to (it.value / totalCount) * 100f
                }.sortedByDescending { it.second }
                favoriteGenrePercentages.value = percentages
            }
        }
    }

    // --- Highly Custom Realtime Synthesis Tone Generator ---
    private fun startSynthesizer() {
        synthJob?.cancel()
        synthJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT
            )

            // Dynamic safe build of AudioTrack for local playback
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.play()

            val bufferSize = 2048
            val buffer = ByteArray(bufferSize)
            var phase = 0.0

            while (isActive && isPlaying.value) {
                val currentPresetId = currentPlayingItem.value?.coverPresetId ?: 0
                val eqFactor = equalizerBands.value

                // Compute synthesized notes dynamically
                // Bass frequencies controlled by EQ low band, higher harmonics controlled by EQ high band
                val baseFreq = when (currentPresetId) {
                    0 -> 130.81 // C3 Drone for Synthwave
                    1 -> 146.83 // D3 Drone for Ambient
                    2 -> 164.81 // E3 Drone for Chillstep
                    3 -> 110.00 // A2 Drone for Electro
                    else -> 196.00 // G3 Drone
                }

                val speedMultiplier = playbackSpeed.value
                val scaledFreq = baseFreq * speedMultiplier

                for (i in 0 until bufferSize) {
                    // Fundamental Sine wave
                    val angle = phase * 2.0 * Math.PI
                    var sample = sin(angle)

                    // Soft Sub-bass harmonic influenced by EQ bass band
                    val bassVolume = (eqFactor[0].toFloat() / 100f) * 0.6f
                    sample += sin(angle * 0.5) * bassVolume

                    // Sparkling high harmonic influenced by EQ treble bands
                    val trebleVolume = (eqFactor[4].toFloat() / 100f) * 0.3f
                    sample += sin(angle * 2.5) * trebleVolume

                    // Warm harmonic
                    sample += sin(angle * 1.5) * 0.15

                    // Warm clipping limit
                    val normalizedSample = (sample / (1.0 + bassVolume + trebleVolume)) * 127.0
                    buffer[i] = (normalizedSample + 128.0).toInt().toByte()

                    phase += scaledFreq / sampleRate
                    if (phase > 1.0) phase -= 1.0
                }

                audioTrack.write(buffer, 0, bufferSize)
                yield()
            }

            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore safe errors on thread stop
            }
        }
    }

    private fun stopSynthesizer() {
        synthJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopSynthesizer()
        playbackProgressJob?.cancel()
        sleepTimerJob?.cancel()
    }
}
