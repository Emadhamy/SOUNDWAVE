package com.soundwave.player.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.player.domain.model.*
import com.soundwave.player.domain.repository.MusicRepository
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val genres: List<String> = emptyList(),
    val folders: List<String> = emptyList(),
    val sortBy: SortBy = SortBy.TITLE,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    
    val playerState = musicPlayer.playerState
    
    init {
        loadLibrary()
    }
    
    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    musicRepository.getAllSongs(),
                    musicRepository.getAllAlbums(),
                    musicRepository.getAllArtists(),
                    musicRepository.getAllPlaylists()
                ) { songs, albums, artists, playlists ->
                    val genres = songs.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()
                    val folders = songs.map { it.folder }.distinct().sorted()
                    
                    LibraryUiState(
                        isLoading = false,
                        songs = sortSongs(songs, _uiState.value.sortBy, _uiState.value.sortOrder),
                        albums = albums,
                        artists = artists,
                        playlists = playlists,
                        genres = genres,
                        folders = folders
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
    
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicPlayer.playSongs(songs, startIndex)
    }
    
    fun shuffleAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            musicPlayer.playSongs(songs.shuffled())
        }
    }
    
    fun setSortBy(sortBy: SortBy) {
        val newOrder = if (_uiState.value.sortBy == sortBy) {
            if (_uiState.value.sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING 
            else SortOrder.ASCENDING
        } else {
            SortOrder.ASCENDING
        }
        
        _uiState.update { state ->
            state.copy(
                sortBy = sortBy,
                sortOrder = newOrder,
                songs = sortSongs(state.songs, sortBy, newOrder)
            )
        }
    }
    
    private fun sortSongs(songs: List<Song>, sortBy: SortBy, order: SortOrder): List<Song> {
        val sorted = when (sortBy) {
            SortBy.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortBy.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SortBy.ALBUM -> songs.sortedBy { it.album.lowercase() }
            SortBy.DATE_ADDED -> songs.sortedBy { it.dateAdded }
            SortBy.DURATION -> songs.sortedBy { it.duration }
            SortBy.PLAY_COUNT -> songs.sortedBy { it.playCount }
        }
        
        return if (order == SortOrder.DESCENDING) sorted.reversed() else sorted
    }
    
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            musicRepository.createPlaylist(name, description)
        }
    }
    
    fun refresh() {
        loadLibrary()
    }
}