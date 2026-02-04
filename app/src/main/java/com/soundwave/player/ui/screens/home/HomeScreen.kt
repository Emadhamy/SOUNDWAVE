package com.soundwave.player.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.domain.model.Song
import com.soundwave.player.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToLibrary: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.greeting,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navigate to search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "بحث")
                    }
                    IconButton(onClick = { /* Navigate to settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Quick Actions
                        item {
                            QuickActionsRow(
                                onShuffleAll = { viewModel.shuffleAll() },
                                onPlayFavorites = { viewModel.playFavorites() }
                            )
                        }
                        
                        // Recently Played
                        if (uiState.recentlyPlayed.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "استمعت مؤخراً",
                                    onViewAllClick = onNavigateToLibrary
                                )
                            }
                            
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.recentlyPlayed) { song ->
                                        RecentSongCard(
                                            song = song,
                                            isPlaying = playerState.currentSong?.id == song.id,
                                            onClick = { viewModel.playSong(song) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Recently Added
                        if (uiState.recentlyAdded.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "أُضيفت حديثاً",
                                    onViewAllClick = onNavigateToLibrary
                                )
                            }
                            
                            itemsIndexed(
                                items = uiState.recentlyAdded.take(5),
                                key = { _, song -> song.id }
                            ) { index, song ->
                                SongItem(
                                    song = song,
                                    isPlaying = playerState.currentSong?.id == song.id,
                                    onClick = {
                                        viewModel.playSongs(uiState.recentlyAdded, index)
                                    },
                                    onMoreClick = { selectedSong = song },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                        
                        // Albums
                        if (uiState.albums.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "الألبومات",
                                    onViewAllClick = onNavigateToLibrary
                                )
                            }
                            
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.albums) { album ->
                                        AlbumItem(
                                            album = album,
                                            onClick = { onNavigateToAlbum(album.id) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Artists
                        if (uiState.artists.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "الفنانون",
                                    onViewAllClick = onNavigateToLibrary
                                )
                            }
                            
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.artists) { artist ->
                                        ArtistItem(
                                            artist = artist,
                                            onClick = { onNavigateToArtist(artist.id) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Playlists
                        if (uiState.playlists.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "قوائم التشغيل",
                                    onViewAllClick = onNavigateToLibrary
                                )
                            }
                            
                            items(uiState.playlists) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    onClick = { onNavigateToPlaylist(playlist.id) },
                                    onMoreClick = { },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                        
                        // Bottom spacing
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
    
    // Song Options Bottom Sheet
    selectedSong?.let { song ->
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSong = null },
            onPlayNext = { /* TODO */ },
            onAddToQueue = { /* TODO */ },
            onAddToPlaylist = { /* TODO */ },
            onToggleFavorite = { /* TODO */ },
            onGoToArtist = { /* TODO */ },
            onGoToAlbum = { /* TODO */ },
            onShare = { /* TODO */ },
            onShowInfo = { /* TODO */ }
        )
    }
}

@Composable
private fun QuickActionsRow(
    onShuffleAll: () -> Unit,
    onPlayFavorites: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Default.Shuffle,
            title = "تشغيل عشوائي",
            onClick = onShuffleAll,
            modifier = Modifier.weight(1f)
        )
        
        QuickActionCard(
            icon = Icons.Default.Favorite,
            title = "المفضلة",
            onClick = onPlayFavorites,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RecentSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(150.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(modifier = Modifier.size(150.dp)) {
                coil.compose.AsyncImage(
                    model = song.artworkUri,
                    contentDescription = song.album,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingIndicator()
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}