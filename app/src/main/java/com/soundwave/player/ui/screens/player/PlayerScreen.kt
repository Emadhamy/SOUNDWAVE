package com.soundwave.player.ui.screens.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soundwave.player.domain.model.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val sleepTimerState by viewModel.sleepTimerState.collectAsState()
    val currentSong = playerState.currentSong
    val visualizerData by viewModel.visualizerData.collectAsState()
    
    // Initialize visualizer with session ID from playerState (if available)
    LaunchedEffect(playerState.audioSessionId) {
        if (playerState.audioSessionId != 0) {
            viewModel.setVisualizerEnabled(true)
        }
    }
    
    var showSpeedSheet by remember { mutableStateOf(false) }
    
    // Animation for rotating album art
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "albumRotation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                var offsetX = 0f
                var offsetY = 0f
                detectDragGestures(
                    onDragStart = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        val absX = kotlin.math.abs(offsetX)
                        val absY = kotlin.math.abs(offsetY)
                        if (absX > absY) {
                            if (absX > 100) {
                                if (offsetX > 0) viewModel.seekToPrevious() else viewModel.seekToNext()
                            }
                        } else {
                            if (offsetY > 150) {
                                onBackClick()
                            } else if (offsetY < -150) {
                                onNavigateToQueue()
                            }
                        }
                    }
                )
            }
    ) {
        // Blurred Background
        currentSong?.artworkUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp)
                    .graphicsLayer { alpha = 0.3f },
                contentScale = ContentScale.Crop
            )
        }
        
        // Gradient Overlay (Transparent Mode)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent, // More transparent mid section
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            PlayerTopBar(
                onBackClick = onBackClick,
                onEqualizerClick = onNavigateToEqualizer,
                onQueueClick = onNavigateToQueue,
                onTimerClick = onNavigateToTimer,
                sleepTimerActive = sleepTimerState.isActive
            )

            // Dynamic Glow behind Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
                    .graphicsLayer { 
                        alpha = if (playerState.isPlaying) 0.5f else 0f
                    }
            ) {
                // Glow effect based on frequencies
                val amplitude = visualizerData.amplitudes.take(10).sum() / 10f
                val glowScale by animateFloatAsState(
                    targetValue = 1f + (amplitude * 0.5f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "glowScale"
                )
                
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center)
                        .graphicsLayer { 
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.3f))
                
                // Album Art
                AlbumArtwork(
                    artworkUri = currentSong?.artworkUri,
                    isPlaying = playerState.isPlaying,
                    rotation = if (playerState.isPlaying) rotation else 0f
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Song Info
                SongInfoSection(
                    title = currentSong?.title ?: "لا توجد أغنية",
                    artist = currentSong?.artist ?: "",
                    isFavorite = isFavorite,
                    onFavoriteClick = { viewModel.toggleFavorite() },
                    onLyricsClick = onNavigateToLyrics
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Progress Bar
                ProgressSection(
                    currentPosition = currentPosition,
                    duration = playerState.duration,
                    onSeek = { viewModel.seekTo(it) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Player Controls
                PlayerControls(
                    isPlaying = playerState.isPlaying,
                    repeatMode = playerState.repeatMode,
                    shuffleEnabled = playerState.shuffleEnabled,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onPrevious = { viewModel.seekToPrevious() },
                    onNext = { viewModel.seekToNext() },
                    onRepeatClick = { viewModel.toggleRepeatMode() },
                    onShuffleClick = { viewModel.toggleShuffle() }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Extra Controls
                ExtraControls(
                    playbackSpeed = playerState.playbackSpeed,
                    onSpeedClick = { showSpeedSheet = true }
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    
    // Speed Bottom Sheet
    if (showSpeedSheet) {
        SpeedBottomSheet(
            currentSpeed = playerState.playbackSpeed,
            onDismiss = { showSpeedSheet = false },
            onSpeedSelected = { speed -> 
                viewModel.setPlaybackSpeed(speed)
                showSpeedSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerTopBar(
    onBackClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onQueueClick: () -> Unit,
    onTimerClick: () -> Unit,
    sleepTimerActive: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = "يشغل الآن",
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "رجوع"
                )
            }
        },
        actions = {
            IconButton(onClick = onTimerClick) {
                Icon(
                    imageVector = if (sleepTimerActive) Icons.Default.Timer else Icons.Default.TimerOff,
                    contentDescription = "مؤقت النوم",
                    tint = if (sleepTimerActive) MaterialTheme.colorScheme.primary 
                           else LocalContentColor.current
                )
            }
            IconButton(onClick = onEqualizerClick) {
                Icon(Icons.Default.Equalizer, contentDescription = "المعادل")
            }
            IconButton(onClick = onQueueClick) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "القائمة")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun AlbumArtwork(
    artworkUri: android.net.Uri?,
    isPlaying: Boolean,
    rotation: Float
) {
    val configuration = LocalConfiguration.current
    val artworkSize = (configuration.screenWidthDp * 0.7).dp
    
    Box(
        modifier = Modifier.size(artworkSize),
        contentAlignment = Alignment.Center
    ) {
        // Gradient Border
        Box(
            modifier = Modifier
                .size(artworkSize)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(4.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = "صورة الألبوم",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // Center Circle (Vinyl effect)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .rotate(rotation)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun SongInfoSection(
    title: String,
    artist: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onLyricsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onLyricsClick) {
                Icon(
                    imageVector = Icons.Filled.Subtitles,
                    contentDescription = "كلمات الأغنية",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val progress = if (duration > 0 && !isDragging) {
        currentPosition.toFloat() / duration.toFloat()
    } else {
        sliderPosition
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { value ->
                isDragging = true
                sliderPosition = value
            },
            onValueChangeFinished = {
                onSeek((sliderPosition * duration).toLong())
                isDragging = false
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(
                    if (isDragging) (sliderPosition * duration).toLong() 
                    else currentPosition
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    repeatMode: RepeatMode,
    shuffleEnabled: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "خلط",
                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Previous
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "السابق",
                modifier = Modifier.size(36.dp)
            )
        }
        
        // Play/Pause
        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier.size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Next
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "التالي",
                modifier = Modifier.size(36.dp)
            )
        }
        
        // Repeat
        IconButton(onClick = onRepeatClick) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                },
                contentDescription = "تكرار",
                tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExtraControls(
    playbackSpeed: Float,
    onSpeedClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onSpeedClick) {
            Text(
                text = "السرعة: ${playbackSpeed}x",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedBottomSheet(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "سرعة التشغيل",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            speeds.forEach { speed ->
                ListItem(
                    headlineContent = { Text("${speed}x") },
                    leadingContent = {
                        RadioButton(
                            selected = speed == currentSpeed,
                            onClick = { onSpeedSelected(speed) }
                        )
                    },
                    modifier = Modifier.clickable { onSpeedSelected(speed) }
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}