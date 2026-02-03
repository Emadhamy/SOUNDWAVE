package com.soundwave.player.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val crossfadeDuration: Int = 0,
    val replayGain: Boolean = false,
    val filterShortSongs: Boolean = true,
    val minSongDuration: Int = 30,
    val showAlbumArtOnLockScreen: Boolean = true,
    val keepScreenOn: Boolean = false,
    val isScanning: Boolean = false,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            // Load statistics
            musicRepository.getAllSongs().collect { songs ->
                _state.update { it.copy(songCount = songs.size) }
            }
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
    }
    
    fun setDynamicColors(enabled: Boolean) {
        _state.update { it.copy(dynamicColors = enabled) }
    }
    
    fun setGaplessPlayback(enabled: Boolean) {
        _state.update { it.copy(gaplessPlayback = enabled) }
    }
    
    fun setCrossfadeDuration(duration: Int) {
        _state.update { it.copy(crossfadeDuration = duration) }
    }
    
    fun setReplayGain(enabled: Boolean) {
        _state.update { it.copy(replayGain = enabled) }
    }
    
    fun setFilterShortSongs(enabled: Boolean) {
        _state.update { it.copy(filterShortSongs = enabled) }
    }
    
    fun setKeepScreenOn(enabled: Boolean) {
        _state.update { it.copy(keepScreenOn = enabled) }
    }
    
    fun scanLibrary() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true) }
            try {
                musicRepository.scanMediaStore()
            } finally {
                _state.update { it.copy(isScanning = false) }
            }
        }
    }
}