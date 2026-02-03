package com.soundwave.player.ui.screens.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soundwave.player.domain.model.Song
import com.soundwave.player.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    onBackClick: () -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(uiState.album?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.loadAlbum(albumId) },
                modifier = Modifier.padding(paddingValues)
            )
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Blurred Background
                    uiState.album?.artworkUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .blur(30.dp)
                                .graphicsLayer { alpha = 0.5f },
                            contentScale = ContentScale.Crop
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = paddingValues
                    ) {
                        // Album Header
                        item {
                            AlbumHeader(
                                album = uiState.album,
                                songCount = uiState.songs.size,
                                totalDuration = uiState.songs.sumOf { it.duration },
                                onPlayAll = { viewModel.playAll() },
                                onShuffle = { viewModel.shuffleAll() }
                            )
                        }
                        
                        // Songs
                        itemsIndexed(
                            items = uiState.songs,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            SongItem(
                                song = song,
                                isPlaying = playerState.currentSong?.id == song.id,
                                showTrackNumber = true,
                                showAlbumArt = false,
                                onClick = { viewModel.playSong(index) },
                                onMoreClick = { selectedSong = song },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
    
    // Song Options
    selectedSong?.let { song ->
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSong = null },
            onPlayNext = { },
            onAddToQueue = { },
            onAddToPlaylist = { },
            onToggleFavorite = { },
            onGoToArtist = { },
            onGoToAlbum = { },
            onShare = { },
            onShowInfo = { }
        )
    }
}

@Composable
private fun AlbumHeader(
    album: com.soundwave.player.domain.model.Album?,
    songCount: Int,
    totalDuration: Long,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album Art
        Card(
            modifier = Modifier.size(200.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AsyncImage(
                model = album?.artworkUri,
                contentDescription = album?.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Album Info
        Text(
            text = album?.artist ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$songCount أغنية • ${formatDuration(totalDuration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (album?.year != null && album.year > 0) {
            Text(
                text = album.year.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("تشغيل")
            }
            
            OutlinedButton(
                onClick = onShuffle,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Shuffle, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("خلط")
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}س ${minutes}د" else "${minutes} دقيقة"
}