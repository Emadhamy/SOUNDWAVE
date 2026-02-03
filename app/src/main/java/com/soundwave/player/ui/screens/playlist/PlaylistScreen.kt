package com.soundwave.player.ui.screens.playlist

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
import com.soundwave.player.domain.model.Song
import com.soundwave.player.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.playlist?.name ?: "")
                        uiState.playlist?.description?.takeIf { it.isNotEmpty() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "تعديل")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "حذف")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.songs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.shuffleAll() },
                    icon = { Icon(Icons.Default.Shuffle, null) },
                    text = { Text("تشغيل عشوائي") }
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            
            uiState.songs.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.MusicNote,
                    title = "لا توجد أغاني",
                    message = "أضف أغاني إلى هذه القائمة",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues
                ) {
                    item {
                        PlaylistHeader(
                            songCount = uiState.songs.size,
                            totalDuration = uiState.songs.sumOf { it.duration },
                            onPlayAll = { viewModel.playAll() }
                        )
                    }
                    
                    itemsIndexed(uiState.songs, key = { _, song -> song.id }) { index, song ->
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
    
    // Edit Dialog
    if (showEditDialog) {
        var name by remember { mutableStateOf(uiState.playlist?.name ?: "") }
        var description by remember { mutableStateOf(uiState.playlist?.description ?: "") }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل القائمة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("الوصف") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updatePlaylist(name, description)
                        showEditDialog = false
                    },
                    enabled = name.isNotBlank()
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("إلغاء") }
            }
        )
    }
    
    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف القائمة") },
            text = { Text("هل أنت متأكد من حذف هذه القائمة؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlaylist()
                    showDeleteDialog = false
                    onBackClick()
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") }
            }
        )
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
private fun PlaylistHeader(
    songCount: Int,
    totalDuration: Long,
    onPlayAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$songCount أغنية • ${formatDuration(totalDuration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(onClick = onPlayAll) {
            Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("تشغيل الكل")
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}س ${minutes}د" else "${minutes} دقيقة"
}