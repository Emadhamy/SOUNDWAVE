package com.soundwave.player.ui.screens.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Artist
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistUiState(
    val isLoading: Boolean = true,
    val artist: Artist? = null,
    val albums: List<Album> = emptyList(),
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    fun loadArtist(artistId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    musicRepository.getArtistById(artistId),
                    musicRepository.getSongsByArtist(artistId)
                ) { artist, songs ->
                    ArtistUiState(
                        isLoading = false,
                        artist = artist,
                        albums = artist?.albums ?: emptyList(),
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