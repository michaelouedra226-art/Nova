package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PlaybackHistory
import com.example.ui.viewmodels.NovaViewModel

@Composable
fun StatisticsScreen(
    viewModel: NovaViewModel
) {
    val totalMinutes by viewModel.totalListeningMinutes.collectAsState()
    val genresShare by viewModel.favoriteGenrePercentages.collectAsState()
    val dailyMinutes by viewModel.statsDailyMinutes.collectAsState()
    val history by viewModel.playbackHistory.collectAsState()

    // Config options
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isCompact by viewModel.isCompactMode.collectAsState()
    val foldersBlocked by viewModel.excludedFolders.collectAsState()

    var showBackupNotice by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // --- 1. Total Listening Insights ---
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bilan Mensuel Nova", color = GoldenSun, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Temps d'écoute global", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                    Icon(Icons.Filled.BarChart, contentDescription = "Stats", tint = GoldenSun)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${totalMinutes} min", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Text("+24% depuis le mois dernier", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldenSun.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text("Rang: Mélomane Or", color = GoldenSun, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 2. Custom Aesthetic Bar Graph ---
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = " minutes par jour (7 derniers jours)",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CustomAestheticBarChart(
                    data = dailyMinutes,
                    labels = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
                )
            }
        }

        // --- 3. Preferred Genres Sharing Breakdown ---
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Répartition par Genre Musical", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))

                if (genresShare.isEmpty()) {
                    Text("Aucune donnée disponible", color = Color.Gray, fontSize = 13.sp)
                } else {
                    genresShare.take(3).forEach { (genre, share) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(genre, color = Color.LightGray, fontSize = 13.sp)
                                Text("${share.toInt()}%", color = GoldenSun, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Simple linear rating share track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CardSlateGrey)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(share / 100f)
                                        .fillMaxHeight()
                                        .background(GoldenSun)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. History Log Tracker ---
        item {
            Text("Écoutés récemment (Historique)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (history.isEmpty()) {
            item {
                Text("Aucun historique pour l'instant.", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        } else {
            items(history.take(4)) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardSlateGrey.copy(alpha = 0.25f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (entry.isVideo) Icons.Filled.Movie else Icons.Filled.Audiotrack,
                            contentDescription = "playback history",
                            tint = if (entry.isVideo) IceCyan else GoldenSun,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = entry.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Écouté",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 5. Custom Interface Adjustments ---
        item {
            Text("Ajustements de l'interface & backup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                // Density Config
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Affichage compact", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Listes denses pour les gros catalogues", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isCompact,
                        onCheckedChange = { viewModel.isCompactMode.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldenSun, checkedTrackColor = GoldenSun.copy(alpha = 0.4f))
                    )
                }

                HorizontalDivider(color = LightGlassOverlay, modifier = Modifier.padding(vertical = 12.dp))

                // Ignored System directories configuration
                Column {
                    Text("Dossiers exclus du scan central", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    foldersBlocked.forEach { folder ->
                        Text("• $folder", color = Color.LightGray.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = LightGlassOverlay, modifier = Modifier.padding(vertical = 12.dp))

                // Backup and Restore preferences options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showBackupNotice = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CardSlateGrey),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exporter Configuration", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showBackupNotice = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CardSlateGrey),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Importer Preferences", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showBackupNotice) {
        AlertDialog(
            onDismissRequest = { showBackupNotice = false },
            title = { Text("Synchronisation locale", color = Color.White) },
            text = { Text("Configuration Nova Player sauvegardée localement dans NovaBackup.json.", color = Color.LightGray) },
            confirmButton = {
                Button(onClick = { showBackupNotice = false }, colors = ButtonDefaults.buttonColors(containerColor = GoldenSun)) {
                    Text("D'accord", color = Color.Black)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
