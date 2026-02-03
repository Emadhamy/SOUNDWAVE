package com.soundwave.player.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.*
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val recentlyPlayed: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    init {
        loadHomeData()
        updateGreeting()
    }
    
    private fun updateGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "صباح الخير ☀️"
            in 12..16 -> "مساء الخير 🌤️"
            in 17..20 -> "مساء النور 🌅"
            else -> "ليلة سعيدة 🌙"
        }
        _uiState.update { it.copy(greeting = greeting) }
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // تحميل البيانات بالتوازي
                combine(
                    musicRepository.getRecentlyPlayedSongs(10),
                    musicRepository.getRecentlyAddedSongs(10),
                    musicRepository.getMostPlayedSongs(10),
                    musicRepository.getFavoriteSongs(),
                    musicRepository.getAllAlbums(),
                    musicRepository.getAllArtists(),
                    musicRepository.getAllPlaylists()
                ) { recent, added, mostPlayed, favorites, albums, artists, playlists ->
                    HomeUiState(
                        isLoading = false,
                        greeting = _uiState.value.greeting,
                        recentlyPlayed = recent,
                        recentlyAdded = added,
                        mostPlayed = mostPlayed,
                        favorites = favorites.take(10),
                        albums = albums.take(10),
                        artists = artists.take(10),
                        playlists = playlists.filter { !it.isSmartPlaylist }
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message) 
                }
            }
        }
    }
    
    fun playSong(song: Song) {
        musicPlayer.playSong(song)
        // تحديث آخر تشغيل
        viewModelScope.launch {
            musicRepository.incrementPlayCount(song.id)
        }
    }
    
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicPlayer.playSongs(songs, startIndex)
    }
    
    fun shuffleAll() {
        val allSongs = _uiState.value.recentlyAdded + _uiState.value.mostPlayed
        if (allSongs.isNotEmpty()) {
            musicPlayer.playSongs(allSongs.shuffled())
        }
    }
    
    fun playFavorites() {
        val favorites = _uiState.value.favorites
        if (favorites.isNotEmpty()) {
            musicPlayer.playSongs(favorites)
        }
    }
    
    fun refresh() {
        loadHomeData()
    }
}