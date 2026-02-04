package com.soundwave.player.ui.screens.lyrics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soundwave.player.ui.components.EmptyState
import com.soundwave.player.ui.components.LoadingState
import com.soundwave.player.ui.screens.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val lyricsState by viewModel.lyricsState.collectAsState()
    
    val listState = rememberLazyListState()
    
    // Auto-scroll to current line
    LaunchedEffect(lyricsState.currentLineIndex) {
        if (lyricsState.lyrics?.isSynced == true && lyricsState.currentLineIndex > 0) {
            listState.animateScrollToItem(
                index = lyricsState.currentLineIndex,
                scrollOffset = -200
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = playerState.currentSong?.title ?: "كلمات الأغنية",
                            style = MaterialTheme.typography.titleMedium
                        )
                        playerState.currentSong?.artist?.let {
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
                }
            )
        }
    ) { paddingValues ->
        when {
            lyricsState.isLoading -> {
                LoadingState(
                    message = "جاري تحميل الكلمات...",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            lyricsState.lyrics == null -> {
                EmptyState(
                    icon = Icons.Filled.Subtitles,
                    title = "لا توجد كلمات",
                    message = lyricsState.error ?: "لم يتم العثور على كلمات لهذه الأغنية",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            else -> {
                val lyrics = lyricsState.lyrics!!
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 32.dp, horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(lyrics.lines) { index, line ->
                        val isCurrentLine = index == lyricsState.currentLineIndex
                        
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (isCurrentLine) 22.sp else 18.sp
                            ),
                            color = if (isCurrentLine) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}