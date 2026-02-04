package com.soundwave.player.data.repository

import android.net.Uri
import com.soundwave.player.data.local.database.dao.SongDao
import com.soundwave.player.data.local.database.entities.SongEntity
import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : MusicRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSongById(id: Long): Flow<Song?> {
        return songDao.getSongByIdFlow(id).map { it?.toDomain() }
    }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return songDao.getSongsByAlbum(albumId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSongsByArtist(artist: String): Flow<List<Song>> {
        return songDao.getSongsByArtist(artist).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSongsByFolder(folderPath: String): Flow<List<Song>> {
        return songDao.getSongsByFolder(folderPath).map { entities -> entities.map { it.toDomain() } }
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAlbumById(albumId: Long): Flow<Album?> {
        return getSongsByAlbum(albumId).map { songs ->
            if (songs.isEmpty()) return@map null
            
            val firstSong = songs.first()
            Album(
                id = firstSong.albumId,
                name = firstSong.album,
                artist = firstSong.artist,
                artworkUri = firstSong.artworkUri,
                songCount = songs.size,
                year = firstSong.year,
                songs = songs
            )
        }
    }

    override suspend fun getSongCount(): Int {
        return songDao.getSongCount()
    }
    
    override suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs.map { it.toEntity() })
    }
    
    override suspend fun deleteAllSongs() {
        songDao.deleteAllSongs()
    }

    // Mapper Extension Functions
    private fun SongEntity.toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumId = albumId,
            duration = duration,
            path = path,
            uri = Uri.parse(uri),
            artworkUri = artworkUri?.let { Uri.parse(it) },
            dateAdded = dateAdded,
            size = size,
            trackNumber = trackNumber,
            year = year,
            genre = genre,
            bitrate = bitrate,
            sampleRate = sampleRate,
            isFavorite = isFavorite,
            playCount = playCount,
            lastPlayed = lastPlayed
        )
    }
    
    private fun Song.toEntity(): SongEntity {
        return SongEntity(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumId = albumId,
            duration = duration,
            path = path,
            uri = uri.toString(),
            artworkUri = artworkUri?.toString(),
            dateAdded = dateAdded,
            size = size,
            trackNumber = trackNumber,
            year = year,
            genre = genre,
            bitrate = bitrate,
            sampleRate = sampleRate,
            isFavorite = isFavorite,
            playCount = playCount,
            lastPlayed = lastPlayed
        )
    }
}
