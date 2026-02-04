package com.soundwave.player.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.model.SortBy
import com.soundwave.player.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToGenre: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    val tabs = listOf("الأغاني", "الألبومات", "الفنانون", "الفولدرات", "القوائم", "الأنواع")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المكتبة") },
                actions = {
                    // Shuffle All
                    IconButton(onClick = { viewModel.shuffleAll() }) {
                        Icon(Icons.Default.Shuffle, "تشغيل عشوائي")
                    }
                    
                    // Sort (only for songs tab)
                    if (pagerState.currentPage == 0) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "ترتيب")
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortBy.entries.forEach { sortBy ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            when (sortBy) {
                                                SortBy.TITLE -> "العنوان"
                                                SortBy.ARTIST -> "الفنان"
                                                SortBy.ALBUM -> "الألبوم"
                                                SortBy.DATE_ADDED -> "تاريخ الإضافة"
                                                SortBy.DURATION -> "المدة"
                                                SortBy.PLAY_COUNT -> "عدد التشغيل"
                                            }
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortBy(sortBy)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.sortBy == sortBy) {
                                            Icon(
                                                imageVector = if (uiState.sortOrder == com.soundwave.player.domain.model.SortOrder.ASCENDING)
                                                    Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> SongsTab(
                        songs = uiState.songs,
                        currentSongId = playerState.currentSong?.id,
                        isLoading = uiState.isLoading,
                        onSongClick = { index -> viewModel.playSong(index) },
                        onSongMoreClick = { song -> selectedSong = song }
                    )
                    1 -> AlbumsTab(
                        albums = uiState.albums,
                        isLoading = uiState.isLoading,
                        onAlbumClick = onNavigateToAlbum
                    )
                    2 -> ArtistsTab(
                        artists = uiState.artists,
                        isLoading = uiState.isLoading,
                        onArtistClick = onNavigateToArtist
                    )
                    3 -> FoldersTab(
                        folders = uiState.folders,
                        isLoading = uiState.isLoading,
                        onFolderClick = onNavigateToFolder
                    )
                    4 -> PlaylistsTab(
                        playlists = uiState.playlists,
                        isLoading = uiState.isLoading,
                        onPlaylistClick = onNavigateToPlaylist,
                        onCreatePlaylist = { showCreatePlaylistDialog = true }
                    )
                    5 -> GenresTab(
                        genres = uiState.genres,
                        isLoading = uiState.isLoading,
                        onGenreClick = onNavigateToGenre
                    )
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
    
    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, description ->
                viewModel.createPlaylist(name, description)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
private fun SongsTab(
    songs: List<Song>,
    currentSongId: Long?,
    isLoading: Boolean,
    onSongClick: (Int) -> Unit,
    onSongMoreClick: (Song) -> Unit
) {
    when {
        isLoading -> LoadingState()
        songs.isEmpty() -> EmptyState(
            icon = Icons.Default.MusicNote,
            title = "لا توجد أغاني",
            message = "لم يتم العثور على أغاني على جهازك"
        )
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "${songs.size} أغنية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    SongItem(
                        song = song,
                        isPlaying = song.id == currentSongId,
                        onClick = { onSongClick(index) },
                        onMoreClick = { onSongMoreClick(song) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun AlbumsTab(
    albums: List<com.soundwave.player.domain.model.Album>,
    isLoading: Boolean,
    onAlbumClick: (Long) -> Unit
) {
    when {
        isLoading -> LoadingState()
        albums.isEmpty() -> EmptyState(
            icon = Icons.Default.Album,
            title = "لا توجد ألبومات",
            message = "لم يتم العثور على ألبومات"
        )
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "${albums.size} ألبوم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(albums, key = { it.id }) { album ->
                    AlbumItemLarge(
                        album = album,
                        onClick = { onAlbumClick(album.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun ArtistsTab(
    artists: List<com.soundwave.player.domain.model.Artist>,
    isLoading: Boolean,
    onArtistClick: (Long) -> Unit
) {
    when {
        isLoading -> LoadingState()
        artists.isEmpty() -> EmptyState(
            icon = Icons.Default.Person,
            title = "لا يوجد فنانون",
            message = "لم يتم العثور على فنانين"
        )
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "${artists.size} فنان",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(artists, key = { it.id }) { artist ->
                    ArtistItemLarge(
                        artist = artist,
                        onClick = { onArtistClick(artist.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<com.soundwave.player.domain.model.Playlist>,
    isLoading: Boolean,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    when {
        isLoading -> LoadingState()
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    CreatePlaylistCard(
                        onClick = onCreatePlaylist,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                if (playlists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد قوائم تشغيل بعد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) },
                            onMoreClick = { },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun FoldersTab(
    folders: List<String>,
    isLoading: Boolean,
    onFolderClick: (String) -> Unit
) {
    when {
        isLoading -> LoadingState()
        folders.isEmpty() -> EmptyState(
            icon = Icons.Default.Folder,
            title = "لا توجد مجلدات",
            message = "لم يتم العثور على مجلدات تحتوي على موسيقى"
        )
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "${folders.size} مجلد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(folders) { folder ->
                    FolderItem(
                        folderPath = folder,
                        onClick = { onFolderClick(folder) },
                        onMoreClick = { },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun FolderItem(
    folderPath: String,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folderName = folderPath.substringAfterLast("/")
    
    ListItem(
        headlineContent = { 
            Text(
                text = folderName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = { 
            Text(
                text = folderPath,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
            }
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun GenresTab(
    genres: List<String>,
    isLoading: Boolean,
    onGenreClick: (String) -> Unit
) {
    when {
        isLoading -> LoadingState()
        genres.isEmpty() -> EmptyState(
            icon = Icons.Default.Category,
            title = "لا توجد أنواع",
            message = "لم يتم العثور على أنواع موسيقية"
        )
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(genres) { genre ->
                    ListItem(
                        headlineContent = { Text(genre) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { onGenreClick(genre) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}