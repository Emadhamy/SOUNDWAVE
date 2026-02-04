package com.soundwave.player.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.data.repository.UserPreferencesRepository
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
    val artistCount: Int = 0,
    val folders: List<String> = emptyList(),
    val excludedFolders: Set<String> = emptySet(),
    val miniPlayerStyle: MiniPlayerStyle = MiniPlayerStyle.DOCKED,
    val accentColor: Int? = null
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class MiniPlayerStyle {
    DOCKED, FLOATING
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadStats()
        observePreferences()
    }
    
    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.dynamicColors.collect { enabled ->
                _state.update { it.copy(dynamicColors = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.filterShortSongs.collect { enabled ->
                _state.update { it.copy(filterShortSongs = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.minSongDuration.collect { duration ->
                _state.update { it.copy(minSongDuration = duration) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.excludedFolders.collect { excluded ->
                _state.update { it.copy(excludedFolders = excluded) }
            }
        }
        viewModelScope.launch {
            musicRepository.getAllFolders().collect { folders ->
                _state.update { it.copy(folders = folders) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.miniPlayerStyle.collect { style ->
                _state.update { it.copy(miniPlayerStyle = style) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.accentColor.collect { color ->
                _state.update { it.copy(accentColor = color) }
            }
        }
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
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }
    
    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColors(enabled)
        }
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
        viewModelScope.launch {
            preferencesRepository.setFilterShortSongs(enabled)
        }
    }

    fun setMinSongDuration(duration: Int) {
        viewModelScope.launch {
            preferencesRepository.setMinSongDuration(duration)
        }
    }
    
    fun setKeepScreenOn(enabled: Boolean) {
        _state.update { it.copy(keepScreenOn = enabled) }
    }
    
    fun setMiniPlayerStyle(style: MiniPlayerStyle) {
        viewModelScope.launch {
            preferencesRepository.setMiniPlayerStyle(style)
        }
    }

    fun setAccentColor(colorArgb: Int?) {
        viewModelScope.launch {
            preferencesRepository.setAccentColor(colorArgb)
        }
    }
    
    fun toggleExcludedFolder(folderPath: String) {
        viewModelScope.launch {
            preferencesRepository.toggleExcludedFolder(folderPath)
        }
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