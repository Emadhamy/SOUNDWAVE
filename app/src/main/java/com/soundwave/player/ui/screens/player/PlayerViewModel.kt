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
    private val sleepTimerManager: SleepTimerManager
) : ViewModel() {
    
    val playerState: StateFlow<PlayerState> = musicPlayer.playerState
    val currentPlaylist: StateFlow<List<Song>> = musicPlayer.currentPlaylist
    val currentPosition: StateFlow<Long> = musicPlayer.currentPosition
    
    val lyricsState = lyricsManager.state
    val sleepTimerState = sleepTimerManager.state
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    init {
        // مراقبة الأغنية الحالية
        viewModelScope.launch {
            playerState.collect { state ->
                state.currentSong?.let { song ->
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
}