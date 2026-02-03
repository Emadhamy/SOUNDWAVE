package com.soundwave.player.ui.screens.player

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.ui.components.EmptyState
import com.soundwave.player.ui.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val playlist by viewModel.currentPlaylist.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قائمة الانتظار") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    if (playlist.isNotEmpty()) {
                        IconButton(onClick = { /* Clear queue */ }) {
                            Icon(Icons.Default.ClearAll, "مسح القائمة")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (playlist.isEmpty()) {
            EmptyState(
                icon = Icons.Default.QueueMusic,
                title = "قائمة الانتظار فارغة",
                message = "أضف أغاني للقائمة من خلال خيار 'إضافة لقائمة الانتظار'",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Now Playing Header
                playerState.currentSong?.let { currentSong ->
                    Text(
                        text = "يشغل الآن",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    SongItem(
                        song = currentSong,
                        isPlaying = true,
                        onClick = { },
                        onMoreClick = { },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Text(
                        text = "التالي في القائمة (${playlist.size - 1})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Queue List
                LazyColumn {
                    itemsIndexed(
                        items = playlist.drop(playerState.currentIndex + 1),
                        key = { index, song -> "${song.id}_$index" }
                    ) { index, song ->
                        val actualIndex = playerState.currentIndex + 1 + index
                        
                        SongItem(
                            song = song,
                            isPlaying = false,
                            onClick = { viewModel.skipToIndex(actualIndex) },
                            onMoreClick = { },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}