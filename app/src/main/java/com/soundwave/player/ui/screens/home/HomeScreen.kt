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
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            uiState.error != null -> ErrorState(
                message = uiState.error ?: "خطأ غير معروف",
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(padding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElevatedCard(
                                onClick = { viewModel.shuffleAll() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Shuffle, null)
                                    Text("تشغيل عشوائي")
                                }
                            }

                            ElevatedCard(
                                onClick = { viewModel.playFavorites() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, null)
                                    Text("المفضلة")
                                }
                            }
                        }
                    }

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

                    if (uiState.recentlyAdded.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "أُضيفت حديثاً",
                                onViewAllClick = onNavigateToLibrary
                            )
                        }
                        itemsIndexed(uiState.recentlyAdded.take(10)) { index, song ->
                            SongItem(
                                song = song,
                                isPlaying = playerState.currentSong?.id == song.id,
                                onClick = { viewModel.playSongs(uiState.recentlyAdded, index) },
                                onMoreClick = { selectedSong = song },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

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
                                    AlbumItem(album = album, onClick = { onNavigateToAlbum(album.id) })
                                }
                            }
                        }
                    }

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
                                    ArtistItem(artist = artist, onClick = { onNavigateToArtist(artist.id) })
                                }
                            }
                        }
                    }

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