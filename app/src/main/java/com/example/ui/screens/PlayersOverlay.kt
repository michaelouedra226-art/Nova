package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.MediaItem
import com.example.ui.viewmodels.NovaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun FullAudioPlayerOverlay(
    viewModel: NovaViewModel,
    onClose: () -> Unit
) {
    val track by viewModel.currentPlayingItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgressMs.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()
    val isShuffle by viewModel.isShuffleEnabled.collectAsState()
    val isRepeatOne by viewModel.isRepeatOneEnabled.collectAsState()
    val eqPreset by viewModel.equalizerPreset.collectAsState()
    val eqBands by viewModel.equalizerBands.collectAsState()
    val queue by viewModel.currentPlayingQueue.collectAsState()

    var showQueueDrawer by remember { mutableStateOf(false) }
    var showEqPanel by remember { mutableStateOf(false) }

    if (track == null) return

    val totalDuration = track!!.durationMs
    val presetId = track!!.coverPresetId

    // Aura atmos environment
    Box(modifier = Modifier.fillMaxSize()) {
        DynamicOverlayAtmosphere(presetId = presetId, isPlaying = isPlaying)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LECTURE EN COURS", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(track!!.album, color = GoldenSun, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                IconButton(onClick = { showEqPanel = !showEqPanel }) {
                    Icon(Icons.Filled.GraphicEq, contentDescription = "Equalizer", tint = if (showEqPanel) GoldenSun else Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cover Art Frame
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nova_cover_placeholder),
                    contentDescription = "Cover Placeholder",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Glassmorphism subtle overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                            )
                        )
                )

                // Interactive Audio visual spectrum lines
                RealtimeEqualizerVisualizer(
                    color = GoldenSun.copy(alpha = 0.8f),
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle Title/Artist details
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track!!.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("full_player_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track!!.artist,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Custom Timeline SEEK Bar Progress
            val displayProgress = progress.coerceAtMost(totalDuration)
            val currentMin = (displayProgress / 60000)
            val currentSec = ((displayProgress % 60000) / 1000)
            val totalMin = (totalDuration / 60000)
            val totalSec = ((totalDuration % 60000) / 1000)

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = displayProgress.toFloat(),
                    onValueChange = { viewModel.seekPlaybackTo(it.toLong()) },
                    valueRange = 0f..totalDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = GoldenSun,
                        activeTrackColor = GoldenSun,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("player_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%02d:%02d", currentMin, currentSec),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format("%02d:%02d", totalMin, totalSec),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.isShuffleEnabled.value = !isShuffle }) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) GoldenSun else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { viewModel.playPrevious() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                // Giant beautiful dynamic play button
                val playInteractionSource = remember { MutableInteractionSource() }
                val isPlayPressed by playInteractionSource.collectIsPressedAsState()
                val playScale by animateFloatAsState(
                    targetValue = if (isPlayPressed) 0.88f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "play_btn_scale"
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = playScale
                            scaleY = playScale
                        }
                        .clip(RoundedCornerShape(100.dp))
                        .background(GoldenSun)
                        .clickable(
                            interactionSource = playInteractionSource,
                            indication = null
                        ) { viewModel.togglePlayPause() }
                        .testTag("full_player_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }

                IconButton(onClick = { viewModel.playNext() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                IconButton(onClick = { viewModel.isRepeatOneEnabled.value = !isRepeatOne }) {
                    Icon(
                        imageVector = if (isRepeatOne) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeatOne) GoldenSun else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom drawer indicators: Queue Drawer & Play Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback speed controller pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            val nextSpeed = when (speed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                2.0f -> 0.75f
                                else -> 1.0f
                            }
                            viewModel.playbackSpeed.value = nextSpeed
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Vitesse : ${speed}x", color = GoldenSun, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Like track
                IconButton(onClick = { viewModel.toggleLikeTrack(track!!.id) }) {
                    Icon(
                        imageVector = if (track!!.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (track!!.isFavorite) Color.Red else Color.White
                    )
                }

                // Open Queue list drawer
                IconButton(onClick = { showQueueDrawer = true }) {
                    Icon(Icons.Filled.Queue, contentDescription = "Queue List", tint = IceCyan)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- EQUALIZER MODAL OVERLAY SHEET ---
        if (showEqPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showEqPanel = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B).copy(alpha = 0.65f),
                                    Color(0xFF0F172A).copy(alpha = 0.50f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Égaliseur Audio 5 Bandes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            IconButton(onClick = { showEqPanel = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                            }
                        }

                        // Presets Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf("Flat", "Pop", "Rock", "Bass Boost")
                            presets.forEach { pr ->
                                val selected = eqPreset == pr
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) GoldenSun else CardSlateGrey)
                                        .clickable { viewModel.selectEqualizerPreset(pr) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pr,
                                        color = if (selected) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Low/High Band adjustments
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val frequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3 kHz", "14 kHz")
                            eqBands.forEachIndexed { index, level ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        Slider(
                                            value = level.toFloat(),
                                            onValueChange = { viewModel.updateSingleEqualizerBand(index, it.toInt()) },
                                            valueRange = 0f..100f,
                                            // Make it vertical slider representation visually or standard
                                            colors = SliderDefaults.colors(thumbColor = GoldenSun, activeTrackColor = GoldenSun)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(frequencies[index], color = Color.LightGray, fontSize = 9.sp)
                                    Text("${level}%", color = GoldenSun, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- QUEUE MUSIC DRAWER OVERLAY ---
        if (showQueueDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable { showQueueDrawer = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = false) {}
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B).copy(alpha = 0.65f),
                                    Color(0xFF0F172A).copy(alpha = 0.50f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = "File D'attente",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (queue.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("La file est vide.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(queue) { index, item ->
                                    val isCurrent = queue.getOrNull(viewModel.currentPlayingIndex.collectAsState().value)?.id == item.id

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCurrent) GoldenSun.copy(alpha = 0.15f) else CardSlateGrey.copy(alpha = 0.3f))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                color = if (isCurrent) GoldenSun else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.artist,
                                                color = Color.LightGray.copy(alpha = 0.6f),
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row {
                                            // Up/Down queue sorting helpers
                                            IconButton(onClick = { viewModel.reorderQueue(index, (index - 1).coerceAtLeast(0)) }) {
                                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Up", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                            }
                                            IconButton(onClick = { viewModel.reorderQueue(index, (index + 1).coerceAtMost(queue.size - 1)) }) {
                                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Down", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                            }
                                            IconButton(onClick = { viewModel.removeFromQueue(item.id) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showQueueDrawer = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenSun),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retour au Lecteur", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullVideoPlayerOverlay(
    video: MediaItem,
    viewModel: NovaViewModel,
    onClose: () -> Unit
) {
    var isControlsVisible by remember { mutableStateOf(true) }
    var currentProgress by remember { mutableStateOf(0L) }
    var isPlayingLocal by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Slide overlay meters
    val brightness by viewModel.swipeBrightnessVal.collectAsState()
    val volume by viewModel.swipeVolumeVal.collectAsState()
    val isLocked by viewModel.isVideoLocked.collectAsState()
    val subtitleSize by viewModel.currentVideoSubtitleSize.collectAsState()
    val subtitleColor by viewModel.currentVideoSubtitleColor.collectAsState()
    val subtitleDelay by viewModel.currentVideoSubtitleDelayMs.collectAsState()
    val screenshotPreview by viewModel.capturedScreenPreview.collectAsState()

    var activeSubtitleText by remember { mutableStateOf("") }
    var isBrightnessActive by remember { mutableStateOf(false) }
    var isVolumeActive by remember { mutableStateOf(false) }

    // Controls Auto-Hide timer
    LaunchedEffect(isControlsVisible, isPlayingLocal) {
        if (isControlsVisible && isPlayingLocal) {
            delay(5000)
            isControlsVisible = false
        }
    }

    // Incremental progress loop of visual particles
    LaunchedEffect(isPlayingLocal) {
        while (isPlayingLocal) {
            delay(200)
            currentProgress += 200
            if (currentProgress >= video.durationMs) {
                currentProgress = 0
            }
            viewModel.saveVideoLastPosition(video.id, currentProgress)

            // Subtitle sync sequence (.srt simulation)
            activeSubtitleText = when {
                currentProgress in 2000L..5000L -> "[ Voix de l'IA ] : Bienvenue à bord de la navette de transport..."
                currentProgress in 8000L..12000L -> "[ Alarme ] : Systèmes de propulsion stabilisés à 94%."
                currentProgress in 15000L..22000L -> "[ Voix ] : NOVA PLAYER est prêt à s'arrimer au module central."
                currentProgress in 26000L..32000L -> "[ Info ] : Chargement des banques de données sonores."
                else -> ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isLocked) {
                detectTapGestures(
                    onTap = {
                        isControlsVisible = !isControlsVisible
                    },
                    onDoubleTap = { offset ->
                        // Double tap left coordinates vs right coordinates for skip!
                        val isRightSide = offset.x > size.width / 2f
                        if (!isLocked) {
                            if (isRightSide) {
                                currentProgress = (currentProgress + 10000).coerceAtMost(video.durationMs)
                            } else {
                                currentProgress = (currentProgress - 10000).coerceAtLeast(0L)
                            }
                        }
                    }
                )
            }
            .pointerInput(isLocked) {
                detectDragGestures(
                    onDragStart = {
                        isBrightnessActive = false
                        isVolumeActive = false
                    },
                    onDragEnd = {
                        isBrightnessActive = false
                        isVolumeActive = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!isLocked) {
                            val isLeftValue = change.position.x < size.width / 2f
                            if (isLeftValue) {
                                isBrightnessActive = true
                                viewModel.swipeBrightnessVal.value = (brightness - dragAmount.y / 8).toInt().coerceIn(1, 100)
                            } else {
                                isVolumeActive = true
                                viewModel.swipeVolumeVal.value = (volume - dragAmount.y / 8).toInt().coerceIn(1, 100)
                            }
                        }
                    }
                )
            }
    ) {
        // --- IMMERSIVE RETINAL RENDERING: Particles Simulation ---
        val brushColors = getPresetColors(video.coverPresetId)
        val infiniteTransition = rememberInfiniteTransition(label = "sim")
        val simOffsetState = infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset"
        )
        val simOffset = simOffsetState.value

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .drawBehind {
                    val drawingWidth = this.size.width
                    val drawingHeight = this.size.height
                    val maxDim = if (drawingWidth > drawingHeight) drawingWidth else drawingHeight

                    // Simulating a hyper visual deep cosmic particle travel
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(brushColors[0].copy(0.35f), Color.Transparent),
                            radius = drawingWidth * simOffset,
                            center = Offset(drawingWidth / 2f, drawingHeight / 2f)
                        )
                    )
                    drawCircle(
                        color = brushColors[1].copy(alpha = 0.2f),
                        radius = maxDim * 0.15f * simOffset,
                        center = Offset(drawingWidth * 0.35f, drawingHeight * 0.45f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Videocam,
                contentDescription = "rendering",
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(120.dp)
            )
        }

        // --- SRT SUBTITLES INCRUSTATION OVERLAY ---
        if (activeSubtitleText.isNotEmpty()) {
            val colorResolved = when(subtitleColor) {
                "Gold" -> GoldenSun
                "Cyan" -> IceCyan
                else -> Color.White
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 80.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = activeSubtitleText,
                    color = colorResolved,
                    fontSize = subtitleSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }
        }

        // --- SWIPE GESTURE FEEDBACK TIMERS ---
        if (isBrightnessActive) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(120.dp, 50.dp)
                    .align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.LightMode, contentDescription = "Brightness", tint = GoldenSun)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lumière: $brightness%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        if (isVolumeActive) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(120.dp, 50.dp)
                    .align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Volume", tint = IceCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vol : $volume%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // --- ACCIDENTAL TOUCH SCREEN LOCK LOCK BAR ---
        if (isLocked) {
            IconButton(
                onClick = { viewModel.isVideoLocked.value = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50.dp))
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = GoldenSun, modifier = Modifier.size(28.dp))
            }
        }

        // --- MEDIA CONTROLLER LAYER PANELS ---
        AnimatedVisibility(
            visible = isControlsVisible && !isLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Dim Overlay background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )

                // Top panel controller
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(video.resolution, color = IceCyan, fontSize = 11.sp)
                    }

                    Row {
                        // Quick click Screenshot module
                        IconButton(onClick = { viewModel.captureVideoScreenshotSimulated() }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Capture Frame", tint = Color.White)
                        }

                        // Local Screen lock controller
                        IconButton(onClick = { viewModel.isVideoLocked.value = true }) {
                            Icon(Icons.Filled.LockOpen, contentDescription = "Lock touches", tint = Color.White)
                        }
                    }
                }

                // Center actions controller: Skip Back, Skip Forward, Play/Pause
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentProgress = (currentProgress - 10000).coerceAtLeast(0) }) {
                        Icon(Icons.Filled.Replay10, contentDescription = "-10", tint = Color.White, modifier = Modifier.size(44.dp))
                    }

                    IconButton(onClick = { isPlayingLocal = !isPlayingLocal }) {
                        Icon(
                            imageVector = if (isPlayingLocal) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
                            contentDescription = "Play/pause local",
                            tint = GoldenSun,
                            modifier = Modifier.size(70.dp)
                        )
                    }

                    IconButton(onClick = { currentProgress = (currentProgress + 10000).coerceAtMost(video.durationMs) }) {
                        Icon(Icons.Filled.Forward10, contentDescription = "+10", tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                }

                // Bottom Panel timeline controller
                val displayProgress = currentProgress.coerceAtMost(video.durationMs)
                val currM = (displayProgress / 60000)
                val currS = ((displayProgress % 60000) / 1000)
                val totM = (video.durationMs / 60000)
                val totS = ((video.durationMs % 60000) / 1000)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(String.format("%02d:%02d", currM, currS), color = Color.White, fontSize = 12.sp)
                        Text(String.format("%02d:%02d", totM, totS), color = Color.White, fontSize = 12.sp)
                    }

                    Slider(
                        value = displayProgress.toFloat(),
                        onValueChange = { currentProgress = it.toLong() },
                        valueRange = 0f..video.durationMs.toFloat(),
                        colors = SliderDefaults.colors(thumbColor = GoldenSun, activeTrackColor = GoldenSun)
                    )
                }
            }
        }

        // --- SCREENSHOT HOVER NOTIFICATION TOAST ---
        screenshotPreview?.let {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEB0F172A))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Wallpaper, contentDescription = "Captured", tint = GoldenSun)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Capture d'écran enregistrée", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Fichier enregistré : /pictures/${video.title}_${currentProgress/1000}s.png", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
