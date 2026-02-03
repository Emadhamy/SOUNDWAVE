package com.soundwave.player.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Artist
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val error: String? = null
) {
    val hasResults: Boolean
        get() = songs.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    private var searchJob: Job? = null
    
    init {
        loadRecentSearches()
    }
    
    fun search(query: String) {
        _uiState.update { it.copy(query = query) }
        
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _uiState.update { 
                it.copy(
                    songs = emptyList(),
                    albums = emptyList(),
                    artists = emptyList(),
                    isLoading = false
                )
            }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val songs = musicRepository.searchSongs(query).first()
                val albums = musicRepository.searchAlbums(query)
                val artists = musicRepository.searchArtists(query)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        songs = songs,
                        albums = albums,
                        artists = artists
                    )
                }
                
                // Save to recent searches
                if (songs.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty()) {
                    saveRecentSearch(query)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun playSong(song: Song) {
        musicPlayer.playSong(song)
    }
    
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicPlayer.playSongs(songs, startIndex)
    }
    
    fun clearSearch() {
        search("")
    }
    
    fun removeRecentSearch(query: String) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches - query)
        }
    }
    
    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }
    
    private fun loadRecentSearches() {
        // Load from DataStore - simplified for now
        _uiState.update { 
            it.copy(recentSearches = listOf()) 
        }
    }
    
    private fun saveRecentSearch(query: String) {
        viewModelScope.launch {
            val current = _uiState.value.recentSearches.toMutableList()
            current.remove(query)
            current.add(0, query)
            _uiState.update { it.copy(recentSearches = current.take(10)) }
        }
    }
}