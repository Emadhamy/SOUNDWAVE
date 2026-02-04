package com.soundwave.player.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderUiState(
    val isLoading: Boolean = true,
    val folderPath: String = "",
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    fun loadFolder(folderPath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, folderPath = folderPath) }
            
            try {
                musicRepository.getSongsByFolder(folderPath).collect { songs ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            songs = songs
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun playSong(index: Int) {
        musicPlayer.playSongs(_uiState.value.songs, index)
    }
    
    fun playAll() {
        if (_uiState.value.songs.isNotEmpty()) {
            musicPlayer.playSongs(_uiState.value.songs)
        }
    }
    
    fun shuffleAll() {
        if (_uiState.value.songs.isNotEmpty()) {
            musicPlayer.playSongs(_uiState.value.songs.shuffled())
        }
    }
}
