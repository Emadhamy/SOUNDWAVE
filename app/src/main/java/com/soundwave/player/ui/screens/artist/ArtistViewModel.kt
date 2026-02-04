package com.soundwave.player.ui.screens.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Artist
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
            try {
                // شجرة التبعية: نحتاج Artist أولاً للحصول على اسمه، ثم نجلب أغانيه
                musicRepository.getArtistById(artistId)
                    .flatMapLatest { artist ->
                        if (artist != null) {
                            musicRepository.getSongsByArtist(artist.name).map { songs ->
                                ArtistUiState(
                                    isLoading = false,
                                    artist = artist,
                                    albums = artist.albums,
                                    songs = songs
                                )
                            }
                        } else {
                            flowOf(ArtistUiState(isLoading = false, error = "لم يتم العثور على الفنان"))
                        }
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun playSong(index: Int) {
        val songs = _uiState.value.songs
        if (index in songs.indices) {
            musicPlayer.playSongs(songs, index)
        }
    }

    fun playAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) musicPlayer.playSongs(songs, 0)
    }

    fun shuffleAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) musicPlayer.playSongs(songs.shuffled(), 0)
    }
}