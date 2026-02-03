package com.soundwave.player.ui.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val isLoading: Boolean = true,
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    musicRepository.getAlbumById(albumId),
                    musicRepository.getSongsByAlbum(albumId)
                ) { album, songs ->
                    AlbumUiState(
                        isLoading = false,
                        album = album,
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
}