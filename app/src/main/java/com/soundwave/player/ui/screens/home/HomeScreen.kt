package com.soundwave.player.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    var selectedSong by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.greeting.ifBlank { "مرحباً" }) },
                actions = {
                    IconButton(onClick = { /* ممكن تروح للبحث */ }) {
                        Icon(Icons.Default.Search, contentDescription = "بحث")
                    }
                    IconButton(onClick = { /* ممكن تروح للإعدادات */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                    }
                }
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