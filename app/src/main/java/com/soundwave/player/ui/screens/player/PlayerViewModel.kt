package com.soundwave.player.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.PlayerState
import com.soundwave.player.domain.model.RepeatMode
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import com.soundwave.player.player.lyrics.LyricsManager
import com.soundwave.player.player.timer.SleepTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicPlayer: MusicPlayer,
    private val musicRepository: MusicRepository,
    private val lyricsManager: LyricsManager,
    private val sleepTimerManager: SleepTimerManager,
    private val visualizerManager: com.soundwave.player.player.visualizer.VisualizerManager,
    private val preferencesRepository: com.soundwave.player.data.repository.UserPreferencesRepository
) : ViewModel() {
    
    val playerState: StateFlow<PlayerState> = musicPlayer.playerState
    val currentPlaylist: StateFlow<List<Song>> = musicPlayer.currentPlaylist
    val currentPosition: StateFlow<Long> = musicPlayer.currentPosition
    
    val lyricsState = lyricsManager.state
    val sleepTimerState = sleepTimerManager.state
    val visualizerData = visualizerManager.data
    val visualizerEnabled = visualizerManager.isEnabled
    
    fun setVisualizerEnabled(enabled: Boolean) {
        visualizerManager.setEnabled(enabled)
    }
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    private val _isMiniPlayerDismissed = MutableStateFlow(false)
    val isMiniPlayerDismissed: StateFlow<Boolean> = _isMiniPlayerDismissed.asStateFlow()

    private val _miniPlayerStyle = MutableStateFlow(com.soundwave.player.ui.screens.settings.MiniPlayerStyle.DOCKED)
    val miniPlayerStyle: StateFlow<com.soundwave.player.ui.screens.settings.MiniPlayerStyle> = _miniPlayerStyle.asStateFlow()
    
    private var lastSongId: Long? = null
    
    init {
        // مراقبة الأغنية الحالية
        viewModelScope.launch {
            playerState.collect { state ->
                state.currentSong?.let { song ->
                    if (song.id != lastSongId) {
                        _isMiniPlayerDismissed.value = false
                        lastSongId = song.id
                    }
                    _isFavorite.value = song.isFavorite
                    // تحميل الكلمات
                    lyricsManager.loadLyrics(song)
                }
            }
        }
        
        // تحديث سطر الكلمات الحالي
        viewModelScope.launch {
            currentPosition.collect { position ->
                lyricsManager.updateCurrentLine(position)
            }
        }

        // مراقبة نمط المشغل المصغر
        viewModelScope.launch {
            preferencesRepository.miniPlayerStyle.collect { style ->
                _miniPlayerStyle.value = style
            }
        }
    }
    
    // ==================== التحكم ====================
    
    fun togglePlayPause() {
        musicPlayer.togglePlayPause()
    }
    
    fun seekTo(position: Long) {
        musicPlayer.seekTo(position)
    }
    
    fun seekToNext() {
        musicPlayer.seekToNext()
    }
    
    fun seekToPrevious() {
        musicPlayer.seekToPrevious()
    }
    
    fun toggleRepeatMode() {
        musicPlayer.toggleRepeatMode()
    }
    
    fun toggleShuffle() {
        musicPlayer.toggleShuffle()
    }
    
    fun setPlaybackSpeed(speed: Float) {
        musicPlayer.setPlaybackSpeed(speed)
    }
    
    // ==================== المفضلة ====================
    
    fun toggleFavorite() {
        viewModelScope.launch {
            playerState.value.currentSong?.let { song ->
                val newStatus = !song.isFavorite
                musicRepository.updateFavoriteStatus(song.id, newStatus)
                _isFavorite.value = newStatus
            }
        }
    }
    
    // ==================== القائمة ====================
    
    fun skipToIndex(index: Int) {
        musicPlayer.skipToIndex(index)
    }
    
    fun removeFromQueue(index: Int) {
        musicPlayer.removeFromQueue(index)
    }
    
    fun moveQueueItem(from: Int, to: Int) {
        musicPlayer.moveQueueItem(from, to)
    }
    
    // ==================== مؤقت النوم ====================
    
    fun setSleepTimer(minutes: Int) {
        sleepTimerManager.setTimer(minutes)
    }
    
    fun cancelSleepTimer() {
        sleepTimerManager.cancel()
    }

    fun dismissMiniPlayer() {
        _isMiniPlayerDismissed.value = true
    }

    fun pausePlayback() {
        musicPlayer.pause()
    }
}