package com.soundwave.player.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.domain.model.Song
import com.soundwave.player.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(
    genreName: String,
    onBackClick: () -> Unit,
    viewModel: GenreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    LaunchedEffect(genreName) {
        viewModel.loadGenre(genreName)
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = genreName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.loadGenre(genreName) },
                modifier = Modifier.padding(paddingValues)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues
                ) {
                    item {
                        GenreHeader(
                            songCount = uiState.songs.size,
                            totalDuration = uiState.songs.sumOf { it.duration },
                            onPlayAll = { viewModel.playAll() },
                            onShuffle = { viewModel.shuffleAll() }
                        )
                    }
                    
                    itemsIndexed(
                        items = uiState.songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        SongItem(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id,
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
private fun GenreHeader(
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
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "$songCount أغنية • ${formatDuration(totalDuration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("تشغيل الكل")
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
