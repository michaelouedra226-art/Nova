package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MediaItem
import com.example.ui.viewmodels.NovaViewModel

@Composable
fun MusicLibraryScreen(
    viewModel: NovaViewModel,
    onOpenPlayer: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenreFilter by viewModel.selectedGenreFilter.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingItem.collectAsState()

    var activeGroupingMode by remember { mutableStateOf("All") } // "All", "Artist", "Album", "Genre", "Year"
    var showEditMetadataDialogFor by remember { mutableStateOf<MediaItem?>(null) }
    var showScanNotification by remember { mutableStateOf(false) }
    var showFilterPanel by remember { mutableStateOf(false) }

    val filteredMusic = remember(items, searchQuery, selectedGenreFilter, activeGroupingMode) {
        items.filter { !it.isVideo }
            .filter {
                if (searchQuery.isEmpty()) true
                else {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true) ||
                    it.tags.contains(searchQuery, ignoreCase = true)
                }
            }
            .filter {
                if (selectedGenreFilter == null) true
                else it.genre.equals(selectedGenreFilter, ignoreCase = true)
            }
            .let { list ->
                // Apply visual sort based on selected grouping tab
                when (activeGroupingMode) {
                    "Artist" -> list.sortedBy { it.artist }
                    "Album" -> list.sortedBy { it.album }
                    "Genre" -> list.sortedBy { it.genre }
                    "Year" -> list.sortedByDescending { it.year }
                    else -> list
                }
            }
    }

    // Genres extracted from current library for dynamic headers
    val allGenres = remember(items) {
        items.filter { !it.isVideo }.map { it.genre }.distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Instant Search Header Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp)
                .testTag("search_input"),
            placeholder = { Text("Rechercher titres, artistes, albums, #tags...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = GoldenSun) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.LightGray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldenSun,
                unfocusedBorderColor = LightGlassOverlay,
                focusedContainerColor = CardSlateGrey.copy(alpha = 0.3f),
                unfocusedContainerColor = CardSlateGrey.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Sleek action row to toggle filter panels and run scans
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredMusic.size} morceaux trouvés",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sleek Filter Toggle Button
                IconButton(
                    onClick = { showFilterPanel = !showFilterPanel },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filtres",
                        tint = if (showFilterPanel || selectedGenreFilter != null || activeGroupingMode != "All") GoldenSun else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(
                    onClick = { showScanNotification = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = IceCyan),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = "Scan", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scanner", fontSize = 12.sp)
                }
            }
        }

        // Animated Collapsible Filters and Sorting Options
        AnimatedVisibility(
            visible = showFilterPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // Sorting Selector Row (Horizontally Scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trier par :", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    val groupings = listOf("All", "Artist", "Album", "Genre", "Year")
                    groupings.forEach { mode ->
                        val selected = activeGroupingMode == mode
                        val bgSelectedColor by animateColorAsState(
                            targetValue = if (selected) GoldenSun else CardSlateGrey.copy(alpha = 0.4f),
                            animationSpec = tween(220),
                            label = "p_bg_color"
                        )
                        val textSelectedColor by animateColorAsState(
                            targetValue = if (selected) Color.Black else Color.LightGray,
                            animationSpec = tween(220),
                            label = "p_txt_color"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.06f else 1.00f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "p_scale"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(30.dp))
                                .background(bgSelectedColor)
                                .clickable { activeGroupingMode = mode }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (mode) {
                                    "All" -> "Tous"
                                    "Artist" -> "Artistes"
                                    "Album" -> "Albums"
                                    "Genre" -> "Genres"
                                    else -> "Années"
                                },
                                color = textSelectedColor,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Genre filtering row (Horizontally Scrollable)
                if (allGenres.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Genre :", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)

                        val isAllFiltered = selectedGenreFilter == null
                        val allFilterBgColor by animateColorAsState(
                            targetValue = if (isAllFiltered) IceCyan.copy(alpha = 0.30f) else Color.Transparent,
                            animationSpec = tween(200),
                            label = "all_filt_bg"
                        )
                        val allFilterTextColor by animateColorAsState(
                            targetValue = if (isAllFiltered) IceCyan else Color.White.copy(alpha = 0.6f),
                            animationSpec = tween(200),
                            label = "all_filt_txt"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(allFilterBgColor)
                                .clickable { viewModel.selectedGenreFilter.value = null }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Tous", color = allFilterTextColor, fontSize = 12.sp)
                        }

                        allGenres.forEach { genre ->
                            val isSelected = selectedGenreFilter?.equals(genre, ignoreCase = true) == true
                            val genreBgColor by animateColorAsState(
                                targetValue = if (isSelected) IceCyan.copy(alpha = 0.30f) else Color.Transparent,
                                animationSpec = tween(200),
                                label = "g_filt_bg"
                            )
                            val genreTextColor by animateColorAsState(
                                targetValue = if (isSelected) IceCyan else Color.White.copy(alpha = 0.6f),
                                animationSpec = tween(200),
                                label = "g_filt_txt"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(genreBgColor)
                                    .clickable { viewModel.selectedGenreFilter.value = genre }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(genre, color = genreTextColor, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (showScanNotification) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0369A1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Done", tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scan en arrière-plan terminé", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("2 nouveaux fichiers audio synchronisés.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    IconButton(onClick = { showScanNotification = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Music Lists
        if (filteredMusic.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.MusicOff,
                        contentDescription = "No music",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aucun morceau ne correspond aux critères", color = Color.White.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("music_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(filteredMusic) { index, track ->
                    // Interactive Track Box
                    GlassmorphicRowItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("track_box_${track.id}"),
                        isFocused = currentPlayingTrack?.id == track.id,
                        onClick = {
                            viewModel.setQueueAndPlay(filteredMusic, index)
                            onOpenPlayer()
                        }
                    ) {
                        // Styled dynamic thumbnail box (optimized size)
                        val colors = getPresetColors(track.coverPresetId)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(colors)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.MusicNote, contentDescription = "piste", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Labels / Metadata Block (occupies all remaining space)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${track.artist} • ${track.album}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Quick tag tags
                            if (track.tags.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    track.tags.split(",").take(2).forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(tag.trim(), color = GoldenSun, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Right hand controls inside row: Compact, highly responsive action icons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { viewModel.toggleLikeTrack(track.id) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (track.isFavorite) Color.Red else Color.White.copy(alpha = 0.45f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { showEditMetadataDialogFor = track }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Metadata",
                                    tint = Color.White.copy(alpha = 0.45f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dynamic Metadata Tag Dialog Editor ---
    showEditMetadataDialogFor?.let { track ->
        var editTitle by remember { mutableStateOf(track.title) }
        var editArtist by remember { mutableStateOf(track.artist) }
        var editAlbum by remember { mutableStateOf(track.album) }
        var editGenre by remember { mutableStateOf(track.genre) }

        AlertDialog(
            onDismissRequest = { showEditMetadataDialogFor = null },
            title = { Text("Édition des métadonnées", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Modifier les informations ID3 du fichier ci-dessous :", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Titre") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldenSun, unfocusedBorderColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        label = { Text("Artiste") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldenSun, unfocusedBorderColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editAlbum,
                        onValueChange = { editAlbum = it },
                        label = { Text("Album") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldenSun, unfocusedBorderColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editGenre,
                        onValueChange = { editGenre = it },
                        label = { Text("Genre") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldenSun, unfocusedBorderColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMediaMetadata(track.id, editTitle, editArtist, editAlbum, editGenre)
                        showEditMetadataDialogFor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenSun)
                ) {
                    Text("Enregistrer", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditMetadataDialogFor = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Annuler")
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
