package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Playlist
import com.example.ui.viewmodels.NovaViewModel

@Composable
fun ControlHubScreen(
    viewModel: NovaViewModel
) {
    val playlists by viewModel.playlists.collectAsState()
    val sleepTimerLeft by viewModel.sleepTimerMinutesLeft.collectAsState()
    val isTimerWarning by viewModel.isSleepTimerWarningActive.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDurationSec.collectAsState()
    val isAlarmEnabled by viewModel.isWakeUpAlarmEnabled.collectAsState()
    val alarmTime by viewModel.wakeUpAlarmTimeString.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var activeTimerSelection by remember { mutableStateOf(15) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // --- SECTION 1: Sleep Timer Component ---
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Minuterie de Sommeil", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Arrêt progressif du son en fin de cycle", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    Icon(
                        Icons.Filled.Bedtime,
                        contentDescription = "Sleep",
                        tint = if (sleepTimerLeft != null) GoldenSun else Color.White.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (sleepTimerLeft != null) {
                    // Operational countdown circle
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${sleepTimerLeft}m restant${if (sleepTimerLeft!! > 1) "s" else ""}",
                            color = GoldenSun,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("sleep_countdown")
                        )

                        if (isTimerWarning) {
                            Text(
                                "🚨 FADE OUT IMINENT : Tap pour repousser",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { viewModel.startSleepTimer(5) }
                            )
                        }

                        Button(
                            onClick = { viewModel.cancelSleepTimer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Text("Annuler la minuterie", color = Color.White)
                        }
                    }
                } else {
                    // Quick dial buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 15, 30, 45, 60).forEach { mins ->
                            val selected = activeTimerSelection == mins
                            
                            val bgSelectedColor by animateColorAsState(
                                targetValue = if (selected) GoldenSun else CardSlateGrey,
                                animationSpec = tween(220),
                                label = "t_bg"
                            )
                            val textSelectedColor by animateColorAsState(
                                targetValue = if (selected) Color.Black else Color.White,
                                animationSpec = tween(220),
                                label = "t_txt"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1.08f else 1.0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "t_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgSelectedColor)
                                    .clickable { activeTimerSelection = mins }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${mins}m",
                                    color = textSelectedColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.startSleepTimer(activeTimerSelection) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenSun),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lancer le décompte", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SECTION 2: Crossfade Configurations ---
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fondu Enchaîné (Crossfade)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Transition acoustique fluide entre morceaux", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.Compress, contentDescription = "Fade", tint = IceCyan)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Slider(
                        value = crossfadeDuration.toFloat(),
                        onValueChange = { viewModel.crossfadeDurationSec.value = it.toInt() },
                        valueRange = 0f..10f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = IceCyan, activeTrackColor = IceCyan)
                    )
                    Text(
                        "${crossfadeDuration}s",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // --- SECTION 3: Smart Wake-Up Alarm Mode ---
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Réveil Musical Intelligent", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Montée en volume progressive à l'alarme", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = { viewModel.isWakeUpAlarmEnabled.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldenSun, checkedTrackColor = GoldenSun.copy(alpha = 0.4f))
                    )
                }

                if (isAlarmEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, contentDescription = "time", tint = GoldenSun)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(alarmTime, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("06:30", "07:30", "08:00").forEach { presetTime ->
                                val selected = alarmTime == presetTime
                                
                                val bgSelectedColor by animateColorAsState(
                                    targetValue = if (selected) GoldenSun else CardSlateGrey,
                                    animationSpec = tween(220),
                                    label = "a_bg"
                                )
                                val textSelectedColor by animateColorAsState(
                                    targetValue = if (selected) Color.Black else Color.LightGray,
                                    animationSpec = tween(220),
                                    label = "a_txt"
                                )
                                val scale by animateFloatAsState(
                                    targetValue = if (selected) 1.08f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                    label = "a_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bgSelectedColor)
                                        .clickable { viewModel.wakeUpAlarmTimeString.value = presetTime }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        presetTime,
                                        color = textSelectedColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 4: Playlists Index ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Playlists de l'utilisateur", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)

                IconButton(
                    onClick = { showCreatePlaylistDialog = true },
                    modifier = Modifier.background(GoldenSun.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New playlist", tint = GoldenSun)
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune playlist créée.", color = Color.White.copy(alpha = 0.4f))
                }
            }
        } else {
            items(playlists) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardSlateGrey.copy(alpha = 0.3f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(GoldenSun, Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.QueueMusic, contentDescription = "Playlist", tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Playlist manuelle", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }

                    IconButton(onClick = { viewModel.deletePlaylist(playlist.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    // CREATE PLAYLIST DIALOG
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Fonder une playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Nom de la playlist (ex: Chill Out)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldenSun, unfocusedBorderColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenSun)
                ) {
                    Text("Créer", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreatePlaylistDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Annuler")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
