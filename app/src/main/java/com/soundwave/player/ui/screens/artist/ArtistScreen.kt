package com.soundwave.player.ui.screens.artist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soundwave.player.domain.model.Song
import com.soundwave.player.ui.components.*
import com.soundwave.player.ui.screens.album.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistId: Long,
    onBackClick: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(uiState.artist?.name ?: "") },
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
                onRetry = { viewModel.loadArtist(artistId) },
                modifier = Modifier.padding(paddingValues)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Artist Header
                    item {
                        ArtistHeader(
                            artist = uiState.artist,
                            songCount = uiState.songs.size,
                            albumCount = uiState.albums.size,
                            onPlayAll = { viewModel.playAll() },
                            onShuffle = { viewModel.shuffleAll() }
                        )
                    }
                    
                    // Albums
                    if (uiState.albums.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "الألبومات",
                                showViewAll = false
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
                    
                    // Songs
                    item {
                        SectionHeader(
                            title = "جميع الأغاني",
                            subtitle = "${uiState.songs.size} أغنية",
                            showViewAll = false
                        )
                    }
                    
                    itemsIndexed(uiState.songs) { index, song ->
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
private fun ArtistHeader(
    artist: com.soundwave.player.domain.model.Artist?,
    songCount: Int,
    albumCount: Int,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(150.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (artist?.artworkUri != null) {
                AsyncImage(
                    model = artist.artworkUri,
                    contentDescription = artist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "$albumCount ألبوم • $songCount أغنية",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onPlayAll, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("تشغيل")
            }
            
            OutlinedButton(onClick = onShuffle, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Shuffle, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("خلط")
            }
        }
    }
}