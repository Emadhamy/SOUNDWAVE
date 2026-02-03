package com.soundwave.player.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.domain.model.Song
import com.soundwave.player.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.search(it) },
                        placeholder = { 
                            Text(
                                "ابحث عن أغنية، فنان، ألبوم...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.large
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                uiState.query.isEmpty() -> {
                    // Recent Searches
                    if (uiState.recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "عمليات البحث الأخيرة",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                    Text("مسح الكل")
                                }
                            }
                        }
                        
                        items(uiState.recentSearches) { search ->
                            ListItem(
                                headlineContent = { Text(search) },
                                leadingContent = {
                                    Icon(Icons.Default.History, contentDescription = null)
                                },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.removeRecentSearch(search) }) {
                                        Icon(Icons.Default.Close, contentDescription = "إزالة")
                                    }
                                },
                                modifier = Modifier.clickable { viewModel.search(search) }
                            )
                        }
                    } else {
                        item {
                            EmptyState(
                                icon = Icons.Default.Search,
                                title = "ابحث عن موسيقاك",
                                message = "ابحث عن الأغاني والألبومات والفنانين"
                            )
                        }
                    }
                }
                
                !uiState.hasResults -> {
                    item {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "لا توجد نتائج",
                            message = "جرب كلمات بحث مختلفة"
                        )
                    }
                }
                
                else -> {
                    // Songs Results
                    if (uiState.songs.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "الأغاني",
                                subtitle = "${uiState.songs.size} نتيجة",
                                showViewAll = uiState.songs.size > 5,
                                onViewAllClick = { }
                            )
                        }
                        
                        itemsIndexed(uiState.songs.take(5)) { index, song ->
                            SongItem(
                                song = song,
                                isPlaying = playerState.currentSong?.id == song.id,
                                onClick = { viewModel.playSongs(uiState.songs, index) },
                                onMoreClick = { selectedSong = song },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    
                    // Albums Results
                    if (uiState.albums.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "الألبومات",
                                subtitle = "${uiState.albums.size} نتيجة",
                                showViewAll = false
                            )
                        }
                        
                        items(uiState.albums.take(3)) { album ->
                            AlbumItemLarge(
                                album = album,
                                onClick = { onNavigateToAlbum(album.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    
                    // Artists Results
                    if (uiState.artists.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "الفنانون",
                                subtitle = "${uiState.artists.size} نتيجة",
                                showViewAll = false
                            )
                        }
                        
                        items(uiState.artists.take(3)) { artist ->
                            ArtistItemLarge(
                                artist = artist,
                                onClick = { onNavigateToArtist(artist.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
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