package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MediaItem
import com.example.ui.viewmodels.NovaViewModel

@Composable
fun VideoLibraryScreen(
    viewModel: NovaViewModel,
    onOpenVideoPlayer: (MediaItem) -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val continueWatchingList by viewModel.continueWatchingList.collectAsState()
    val subtitleSize by viewModel.currentVideoSubtitleSize.collectAsState()
    val subtitleColor by viewModel.currentVideoSubtitleColor.collectAsState()
    val subtitleDelay by viewModel.currentVideoSubtitleDelayMs.collectAsState()
    val isFloatingVideoActive by viewModel.isFloatingVideoActive.collectAsState()
    val currentPlayingItem by viewModel.currentPlayingItem.collectAsState()

    var showSubtitleSettingsDialog by remember { mutableStateOf(false) }

    val videosList = remember(items) {
        items.filter { it.isVideo }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Continue watching horizontal shelf
        if (continueWatchingList.isNotEmpty()) {
            Text(
                text = "Reprendre la lecture",
                color = GoldenSun,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                continueWatchingList.forEach { video ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSlateGrey.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onOpenVideoPlayer(video) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF334155), Color(0xFF0F172A))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PlayCircle, contentDescription = "Play", tint = Color.White.copy(alpha = 0.7f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = video.resolution,
                                color = IceCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Subtitle Config bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vidéos de l'appareil",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showSubtitleSettingsDialog = true }) {
                Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles", tint = IceCyan)
            }
        }

        if (videosList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.VideoFile,
                        contentDescription = "No video",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aucune vidéo détectée.", color = Color.White.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("video_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(videosList) { video ->
                    GlassmorphicRowItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        isFocused = currentPlayingItem?.id == video.id,
                        onClick = { onOpenVideoPlayer(video) }
                    ) {
                        // Custom premium video preview thumbnail box
                        Box(
                            modifier = Modifier
                                .size(90.dp, 60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(
                                        getPresetColors(video.coverPresetId)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = video.resolution,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${video.artist} • ${video.album}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                              ) {
                                // Folder tag
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Sci-Fi Library", color = IceCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("01:30 min", color = GoldenSun, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.isFloatingVideoActive.value = !isFloatingVideoActive }) {
                            Icon(
                                Icons.Filled.PictureInPicture, 
                                contentDescription = "PiP Simulation", 
                                tint = if (isFloatingVideoActive) GoldenSun else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Subtitles Config Dialog Panel ---
    if (showSubtitleSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSubtitleSettingsDialog = false },
            title = { Text("Configuration des sous-titres .SRT", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Personnaliser l'affichage des sous-titres incrustés :", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)

                    // Subtitle alignment size slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Taille des polices", color = Color.LightGray, fontSize = 13.sp)
                            Text("${subtitleSize} sp", color = IceCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Slider(
                            value = subtitleSize.toFloat(),
                            onValueChange = { viewModel.currentVideoSubtitleSize.value = it.toInt() },
                            valueRange = 12f..24f,
                            colors = SliderDefaults.colors(thumbColor = IceCyan, activeTrackColor = IceCyan)
                        )
                    }

                    // Coloring options
                    Column {
                        Text("Couleur d'affichage", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("White", "Gold", "Cyan").forEach { colorName ->
                                val selected = subtitleColor == colorName
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) IceCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { viewModel.currentVideoSubtitleColor.value = colorName }
                                        .border(
                                            width = if (selected) 1.5.dp else 0.dp,
                                            color = if (selected) IceCyan else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(colorName) {
                                            "White" -> "Blanc"
                                            "Gold" -> "Or Doré"
                                            else -> "Cyan Glace"
                                        },
                                        color = when(colorName) {
                                            "White" -> Color.White
                                            "Gold" -> GoldenSun
                                            else -> IceCyan
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Delay synchronization setup
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Délai de synchronisation .srt", color = Color.LightGray, fontSize = 13.sp)
                            Text("${subtitleDelay} ms", color = GoldenSun, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
                            Button(
                                onClick = { viewModel.currentVideoSubtitleDelayMs.value -= 250 },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlateGrey),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("-250ms", color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.currentVideoSubtitleDelayMs.value += 250 },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlateGrey),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+250ms", color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSubtitleSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenSun)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
