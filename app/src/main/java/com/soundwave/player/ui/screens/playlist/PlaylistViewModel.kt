package com.soundwave.player.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.Playlist
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val isLoading: Boolean = true,
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    private var currentPlaylistId: Long = 0
    
    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    musicRepository.getPlaylistById(playlistId),
                    musicRepository.getPlaylistSongs(playlistId)
                ) { playlist, songs ->
                    PlaylistUiState(
                        isLoading = false,
                        playlist = playlist?.copy(songs = songs),
                        songs = songs
                    )
                }.collect { state ->
                    _uiState.value = state
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
    
    fun updatePlaylist(name: String, description: String) {
        viewModelScope.launch {
            _uiState.value.playlist?.let { playlist ->
                musicRepository.updatePlaylist(playlist.copy(name = name, description = description))
            }
        }
    }
    
    fun deletePlaylist() {
        viewModelScope.launch {
            musicRepository.deletePlaylist(currentPlaylistId)
        }
    }
    
    fun removeSong(songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(currentPlaylistId, songId)
        }
    }
}