package com.soundwave.player.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.soundwave.player.domain.model.PlayerState
import com.soundwave.player.domain.model.RepeatMode
import com.soundwave.player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private var originalPlaylist: List<Song> = emptyList()
    private var positionUpdateJob: kotlinx.coroutines.Job? = null
    
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayerState { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState { 
                it.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    error = null
                )
            }
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = mediaController?.currentMediaItemIndex ?: 0
            val song = _currentPlaylist.value.getOrNull(index)
            updatePlayerState { 
                it.copy(
                    currentSong = song,
                    currentIndex = index,
                    duration = mediaController?.duration?.takeIf { d -> d > 0 } ?: 0L,
                    currentPosition = 0L
                )
            }
            _currentPosition.value = 0L
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            val mode = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
            updatePlayerState { it.copy(repeatMode = mode) }
        }
        
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updatePlayerState { it.copy(shuffleEnabled = shuffleModeEnabled) }
            if (shuffleModeEnabled) {
                shufflePlaylist()
            } else {
                restoreOriginalPlaylist()
            }
        }
        
        override fun onPlayerError(error: PlaybackException) {
            updatePlayerState { it.copy(error = error.message) }
        }
        
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _currentPosition.value = newPosition.positionMs
            updatePlayerState { it.copy(currentPosition = newPosition.positionMs) }
        }
    }
    
    fun connect() {
        if (mediaController != null) return
        
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
            
            // استعادة الحالة إذا كان هناك تشغيل جاري
            mediaController?.let { controller ->
                if (controller.mediaItemCount > 0) {
                    updatePlayerState {
                        it.copy(
                            isPlaying = controller.isPlaying,
                            currentPosition = controller.currentPosition,
                            duration = controller.duration.takeIf { d -> d > 0 } ?: 0L,
                            audioSessionId = getAudioSessionId()
                        )
                    }
                }
            }
        }, MoreExecutors.directExecutor())
    }
    
    fun disconnect() {
        stopPositionUpdates()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
    
    // ==================== التشغيل ====================
    
    fun playSong(song: Song) {
        playSongs(listOf(song), 0)
    }
    
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        
        originalPlaylist = songs
        _currentPlaylist.value = songs
        
        val mediaItems = songs.map { it.toMediaItem() }
        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }
        
        updatePlayerState { 
            it.copy(
                currentSong = songs.getOrNull(startIndex),
                currentIndex = startIndex,
                playlist = songs,
                error = null
            )
        }
    }
    
    fun addToQueue(song: Song) {
        mediaController?.addMediaItem(song.toMediaItem())
        _currentPlaylist.update { it + song }
        originalPlaylist = originalPlaylist + song
    }
    
    fun addToQueue(songs: List<Song>) {
        val mediaItems = songs.map { it.toMediaItem() }
        mediaController?.addMediaItems(mediaItems)
        _currentPlaylist.update { it + songs }
        originalPlaylist = originalPlaylist + songs
    }
    
    fun playNext(song: Song) {
        val currentIndex = mediaController?.currentMediaItemIndex ?: 0
        mediaController?.addMediaItem(currentIndex + 1, song.toMediaItem())
        _currentPlaylist.update { 
            it.toMutableList().apply { add(currentIndex + 1, song) }
        }
    }
    
    // ==================== التحكم ====================
    
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }
    
    fun stop() {
        mediaController?.stop()
        updatePlayerState { PlayerState() }
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
        updatePlayerState { it.copy(currentPosition = position) }
    }
    
    fun seekToNext() {
        mediaController?.seekToNext()
    }
    
    fun seekToPrevious() {
        val currentPos = mediaController?.currentPosition ?: 0
        if (currentPos > 3000) {
            // إذا مر أكثر من 3 ثواني، ارجع لبداية الأغنية
            seekTo(0)
        } else {
            mediaController?.seekToPrevious()
        }
    }
    
    fun skipToIndex(index: Int) {
        if (index in 0 until (mediaController?.mediaItemCount ?: 0)) {
            mediaController?.seekTo(index, 0)
        }
    }
    
    fun removeFromQueue(index: Int) {
        if (index in 0 until (mediaController?.mediaItemCount ?: 0)) {
            mediaController?.removeMediaItem(index)
            _currentPlaylist.update { 
                it.toMutableList().apply { removeAt(index) }
            }
        }
    }
    
    fun clearQueue() {
        mediaController?.clearMediaItems()
        _currentPlaylist.value = emptyList()
        originalPlaylist = emptyList()
        updatePlayerState { PlayerState() }
    }
    
    fun moveQueueItem(from: Int, to: Int) {
        mediaController?.moveMediaItem(from, to)
        _currentPlaylist.update { 
            it.toMutableList().apply { 
                add(to, removeAt(from)) 
            }
        }
    }
    
    // ==================== أوضاع التشغيل ====================
    
    fun setRepeatMode(mode: RepeatMode) {
        val repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        mediaController?.repeatMode = repeatMode
    }
    
    fun toggleRepeatMode() {
        val currentMode = _playerState.value.repeatMode
        setRepeatMode(currentMode.next())
    }
    
    fun setShuffleEnabled(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
    }
    
    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        updatePlayerState { it.copy(playbackSpeed = speed) }
    }
    
    fun setVolume(volume: Float) {
        mediaController?.volume = volume.coerceIn(0f, 1f)
        updatePlayerState { it.copy(volume = volume) }
    }
    
    // ==================== المساعدة ====================
    
    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return mediaController?.duration?.takeIf { it > 0 } ?: 0L
    }
    
    fun getAudioSessionId(): Int {
        return mediaController?.let {
            // نحتاج الوصول للـ ExoPlayer مباشرة للحصول على audioSessionId
            0
        } ?: 0
    }
    
    private fun updatePlayerState(update: (PlayerState) -> PlayerState) {
        _playerState.update(update)
    }
    
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (true) {
                val position = mediaController?.currentPosition ?: 0L
                val duration = mediaController?.duration?.takeIf { it > 0 } ?: 0L
                _currentPosition.value = position
                updatePlayerState { 
                    it.copy(
                        currentPosition = position,
                        duration = duration
                    )
                }
                delay(200) // تحديث كل 200ms
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    private fun shufflePlaylist() {
        val currentSong = _playerState.value.currentSong
        val shuffled = _currentPlaylist.value.toMutableList()
        shuffled.shuffle()
        // ضع الأغنية الحالية في البداية
        currentSong?.let { song ->
            shuffled.remove(song)
            shuffled.add(0, song)
        }
        _currentPlaylist.value = shuffled
    }
    
    private fun restoreOriginalPlaylist() {
        _currentPlaylist.value = originalPlaylist
    }
    
    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(artworkUri)
                    .setTrackNumber(trackNumber)
                    .setRecordingYear(year)
                    .setGenre(genre)
                    .build()
            )
            .build()
    }
}