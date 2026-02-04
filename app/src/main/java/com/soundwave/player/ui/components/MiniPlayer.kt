package com.soundwave.player.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlin.math.roundToInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soundwave.player.ui.screens.player.PlayerViewModel
import com.soundwave.player.ui.screens.settings.MiniPlayerStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val currentSong = playerState.currentSong
    
    val isDismissed by viewModel.isMiniPlayerDismissed.collectAsState()
    val miniPlayerStyle by viewModel.miniPlayerStyle.collectAsState()
    val isFloating = miniPlayerStyle == MiniPlayerStyle.FLOATING
    
    val isYouTubeStyle = miniPlayerStyle == MiniPlayerStyle.YOUTUBE
    
    if (isYouTubeStyle && currentSong != null && !isDismissed) {
        YouTubeMiniPlayer(
            song = currentSong,
            isPlaying = playerState.isPlaying,
            onClick = onClick,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            viewModel = viewModel
        )
    } else {
        AnimatedVisibility(
            visible = currentSong != null && !isDismissed,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = modifier // This modifier comes from NavGraph
                .padding(bottom = 0.dp) // Reset any padding
        ) {
            currentSong?.let { song ->
                // Existing Bar Style (DOCKED/FLOATING)
                Surface(
                    modifier = Modifier
                        .padding(
                            horizontal = if (isFloating) 16.dp else 4.dp,
                            vertical = if (isFloating) 8.dp else 0.dp
                        )
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount: Float ->
                                    if (dragAmount > 50f) { // Threshold for swipe down
                                        viewModel.pausePlayback()
                                        viewModel.dismissMiniPlayer()
                                    }
                                }
                            )
                        }
                        .clip(RoundedCornerShape(if (isFloating) 24.dp else 12.dp)),
                    color = if (isFloating) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = if (isFloating) 8.dp else 4.dp,
                    shadowElevation = if (isFloating) 12.dp else 6.dp
                ) {
                    Column {
                        // Progress Indicator
                        LinearProgressIndicator(
                            progress = { playerState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onClick)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Album Art
                            Card(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AsyncImage(
                                    model = song.artworkUri,
                                    contentDescription = song.album,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            // Song Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                                
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                            
                            // Control Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous
                                IconButton(
                                    onClick = { viewModel.seekToPrevious() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "السابق",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // Play/Pause
                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (playerState.isPlaying) {
                                            Icons.Default.Pause
                                        } else {
                                            Icons.Default.PlayArrow
                                        },
                                        contentDescription = if (playerState.isPlaying) "إيقاف" else "تشغيل",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                // Next
                                IconButton(
                                    onClick = { viewModel.seekToNext() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "التالي",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YouTubeMiniPlayer(
    song: com.soundwave.player.domain.model.Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    viewModel: PlayerViewModel
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val sW: Float = constraints.maxWidth.toFloat()
        val sH: Float = constraints.maxHeight.toFloat()
        val pS: Float = with(density) { 120.dp.toPx() }
        
        // Initial position Bottom-Right
        LaunchedEffect(Unit) {
            offsetX = sW - pS - 40f
            offsetY = sH - pS - 200f
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp))
                .pointerInput(Unit) {
                    val sizePx = pS
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, sW - sizePx)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, sH - sizePx)
                    }
                }
                .clickable { onClick() }
        ) {
            // Album Art as Background
            AsyncImage(
                model = song.artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )
            
            // Glass Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Play/Pause Overlay
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Close Button
            IconButton(
                onClick = { viewModel.dismissMiniPlayer() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}