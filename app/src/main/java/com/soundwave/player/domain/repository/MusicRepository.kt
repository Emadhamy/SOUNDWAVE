package com.soundwave.player.domain.repository

import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Artist
import com.soundwave.player.domain.model.Playlist
import com.soundwave.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    
    // Songs
    fun getAllSongs(): Flow<List<Song>>
    fun getSongById(id: Long): Flow<Song?>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artist: String): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    fun getSongsByGenre(genre: String): Flow<List<Song>>
    fun getAllFolders(): Flow<List<String>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun getRecentlyPlayedSongs(limit: Int): Flow<List<Song>>
    fun getRecentlyAddedSongs(limit: Int): Flow<List<Song>>
    fun getMostPlayedSongs(limit: Int): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    
    // Albums
    fun getAllAlbums(): Flow<List<Album>>
    fun getAlbumById(albumId: Long): Flow<Album?>
    fun searchAlbums(query: String): Flow<List<Album>>
    
    // Artists
    fun getAllArtists(): Flow<List<Artist>>
    fun getArtistById(artistId: Long): Flow<Artist?>
    fun searchArtists(query: String): Flow<List<Artist>>
    
    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(id: Long): Flow<Playlist?>
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String, description: String)
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    // Actions & Stats
    suspend fun getSongCount(): Int
    suspend fun insertSongs(songs: List<Song>)
    suspend fun deleteAllSongs()
    suspend fun incrementPlayCount(songId: Long)
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    suspend fun scanMediaStore()
}
