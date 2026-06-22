package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.MediaItem
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.NovaViewModel
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: NovaViewModel = viewModel()

                var activeTab by remember { mutableStateOf("Music") } // "Music", "Video", "Hub", "Stats"
                var showFullAudioPlayer by remember { mutableStateOf(false) }
                var selectedVideoToPlay by remember { mutableStateOf<MediaItem?>(null) }

                // Collect states for persistent dynamic mini-player
                val currentPlayingTrack by viewModel.currentPlayingItem.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val progressMs by viewModel.playbackProgressMs.collectAsState()

                // Custom Picture in Picture coordinates
                val isPiPActive by viewModel.isFloatingVideoActive.collectAsState()
                var pipX by remember { mutableStateOf(30f) }
                var pipY by remember { mutableStateOf(400f) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ObsidianSpace, // Luxury Obsidian Deep Space background
                    bottomBar = {
                        // Glassmorphic modern floating navigation bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF1E293B).copy(alpha = 0.55f),
                                                Color(0xFF0F172A).copy(alpha = 0.35f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.20f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(vertical = 11.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tabs = listOf(
                                    Triple("Music", "Musique", Icons.Filled.LibraryMusic),
                                    Triple("Video", "Vidéo", Icons.Filled.VideoLibrary),
                                    Triple("Hub", "Hub", Icons.Filled.Layers),
                                    Triple("Stats", "Stats", Icons.Filled.Assessment)
                                )

                                tabs.forEach { tab ->
                                    val tabId = tab.first
                                    val tabLabel = tab.second
                                    val icon = tab.third
                                    val isSelected = activeTab == tabId

                                    // Soft premium transitions (similar to CSS style)
                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.08f else 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "tab_scale"
                                    )
                                    val activeBgAlpha by animateFloatAsState(
                                        targetValue = if (isSelected) 0.12f else 0.0f,
                                        animationSpec = tween(250, easing = LinearOutSlowInEasing),
                                        label = "tab_bg"
                                    )
                                    val animatedTint by animateColorAsState(
                                        targetValue = if (isSelected) GoldenSun else Color.White.copy(alpha = 0.45f),
                                        animationSpec = tween(200),
                                        label = "tab_tint"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = activeBgAlpha))
                                            .clickable { activeTab = tabId }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                            .testTag("nav_${tabId.lowercase()}")
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = tabLabel,
                                            tint = animatedTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tabLabel,
                                            color = animatedTint,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Atmosphere background dynamic glassmorphic orbs
                        AmbientGlassyBackground()

                        // Top brand title strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Nova", tint = GoldenSun, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NOVA PLAYER",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                            }

                            // Dynamic preset header badges matching track color presets
                            val activePresetId = currentPlayingTrack?.coverPresetId ?: 0
                            val activePresetLabel = when (activePresetId) {
                                0 -> "Synthwave Active"
                                1 -> "Ambient Calm"
                                2 -> "Zen Waves"
                                3 -> "Midnight Electro"
                                else -> "Normal EQ"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(getPresetColors(activePresetId)[0].copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(activePresetLabel, color = getPresetColors(activePresetId)[0], fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Tab Display Switcher
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 60.dp)
                        ) {
                            when (activeTab) {
                                "Music" -> MusicLibraryScreen(
                                    viewModel = viewModel,
                                    onOpenPlayer = { showFullAudioPlayer = true }
                                )
                                "Video" -> VideoLibraryScreen(
                                    viewModel = viewModel,
                                    onOpenVideoPlayer = { selectedVideoToPlay = it }
                                )
                                "Hub" -> ControlHubScreen(
                                    viewModel = viewModel
                                )
                                "Stats" -> StatisticsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }

                        // --- 1. PERSISTENT FLOATING AUDIO MINI-PLAYER (Visible everywhere) ---
                        if (currentPlayingTrack != null && !showFullAudioPlayer && selectedVideoToPlay == null) {
                            val miniInteractionSource = remember { MutableInteractionSource() }
                            val isMiniPressed by miniInteractionSource.collectIsPressedAsState()
                            val miniScale by animateFloatAsState(
                                targetValue = if (isMiniPressed) 0.96f else 1.0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "mini_scale"
                            )
                            val miniBorderAlpha by animateFloatAsState(
                                targetValue = if (isMiniPressed) 0.38f else 0.22f,
                                animationSpec = tween(150),
                                label = "mini_border"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = miniScale
                                            scaleY = miniScale
                                        }
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF1E293B).copy(alpha = 0.60f),
                                                    Color(0xFF0F172A).copy(alpha = 0.40f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = miniBorderAlpha),
                                                    Color.White.copy(alpha = 0.04f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable(
                                            interactionSource = miniInteractionSource,
                                            indication = androidx.compose.foundation.LocalIndication.current,
                                            onClick = { showFullAudioPlayer = true }
                                        )
                                        .padding(10.dp)
                                        .testTag("mini_player_bar"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Album mini cover circle
                                    val colors = getPresetColors(currentPlayingTrack!!.coverPresetId)
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brush.linearGradient(colors))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = "Piste",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.Center)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Rolling title text
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentPlayingTrack!!.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = currentPlayingTrack!!.artist,
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Mini linear slider progression representation
                                        val totalDuration = currentPlayingTrack!!.durationMs
                                        val ratio = if (totalDuration > 0) progressMs.toFloat() / totalDuration.toFloat() else 0f
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(ratio.coerceAtMost(1f))
                                                    .fillMaxHeight()
                                                    .background(GoldenSun)
                                            )
                                        }
                                    }

                                    // Quick mini playback toggles
                                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "playpause",
                                            tint = Color.White
                                        )
                                    }

                                    IconButton(onClick = { viewModel.playNext() }) {
                                        Icon(
                                            imageVector = Icons.Filled.SkipNext,
                                            contentDescription = "seeknext",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // --- 2. FLOATING PICTURE IN PICTURE DRAGGABLE WINDOW (Simulated in-app) ---
                        if (isPiPActive) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(pipX.roundToInt(), pipY.roundToInt()) }
                                    .size(160.dp, 100.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xE61E293B))
                                    .border(1.dp, IceCyan, RoundedCornerShape(14.dp))
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            pipX += dragAmount.x
                                            pipY += dragAmount.y
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Draggable PiP", color = IceCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = { viewModel.isFloatingVideoActive.value = false },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Mini rendering simulator
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Movie, contentDescription = "Active video", tint = GoldenSun, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Nova stream...", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // --- 3. THE COMPLETE AUDIO PLAYER SCREEN ---
                        AnimatedVisibility(
                            visible = showFullAudioPlayer,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                FullAudioPlayerOverlay(
                                    viewModel = viewModel,
                                    onClose = { showFullAudioPlayer = false }
                                )
                            }
                        }

                        // --- 4. THE IMMERSIVE FULLSCREEN VIDEO PLAYER ---
                        selectedVideoToPlay?.let { videoItem ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                            ) {
                                FullVideoPlayerOverlay(
                                    video = videoItem,
                                    viewModel = viewModel,
                                    onClose = { selectedVideoToPlay = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
