package com.soundwave.player.domain.repository

import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    
    // Songs
    fun getAllSongs(): Flow<List<Song>>
    fun getSongById(id: Long): Flow<Song?>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artist: String): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    
    // Albums
    fun getAlbumById(albumId: Long): Flow<Album?>
    
    // Other
    suspend fun getSongCount(): Int
    suspend fun insertSongs(songs: List<Song>)
    suspend fun deleteAllSongs()
}
