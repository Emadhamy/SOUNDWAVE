package com.soundwave.player.data.repository

import android.net.Uri
import com.soundwave.player.data.local.database.dao.PlaylistDao
import com.soundwave.player.data.local.database.dao.SongDao
import com.soundwave.player.data.local.database.entities.PlaylistEntity
import com.soundwave.player.data.local.database.entities.SongEntity
import com.soundwave.player.domain.model.Album
import com.soundwave.player.domain.model.Artist
import com.soundwave.player.domain.model.Playlist
import com.soundwave.player.domain.model.Song
import com.soundwave.player.domain.repository.MusicRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) : MusicRepository {

    // ==================== Songs ====================

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

    override fun getRecentlyPlayedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getRecentlyPlayedSongs(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getRecentlyAddedSongs(limit: Int): Flow<List<Song>> {
         return songDao.getRecentlyAddedSongs(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMostPlayedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getMostPlayedSongs(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { entities -> entities.map { it.toDomain() } }
    }

    // ==================== Albums ====================

    override fun getAllAlbums(): Flow<List<Album>> {
        return songDao.getAllSongs().map { songs ->
            songs.groupBy { it.albumId }.map { (albumId, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = albumId,
                    name = firstSong.album,
                    artist = firstSong.artist,
                    artworkUri = firstSong.artworkUri?.let { Uri.parse(it) },
                    songCount = albumSongs.size,
                    year = firstSong.year,
                    songs = albumSongs.map { it.toDomain() }
                )
            }.sortedBy { it.name }
        }
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
    
    override fun searchAlbums(query: String): Flow<List<Album>> {
        return getAllAlbums().map { albums ->
            albums.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    // ==================== Artists ====================

    override fun getAllArtists(): Flow<List<Artist>> {
         return songDao.getAllSongs().map { songs ->
            songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
                val albums = artistSongs.groupBy { it.albumId }.map { (_, albumSongs) ->
                     val firstSong = albumSongs.first()
                     Album(
                        id = firstSong.albumId,
                        name = firstSong.album,
                        artist = firstSong.artist,
                        artworkUri = firstSong.artworkUri?.let { Uri.parse(it) },
                        songCount = albumSongs.size,
                        year = firstSong.year,
                        songs = albumSongs.map { it.toDomain() }
                    )
                }
                
                Artist(
                    id = artistName.hashCode().toLong(), // Simple ID generation
                    name = artistName,
                    albumCount = albums.size,
                    songCount = artistSongs.size,
                    artworkUri = null, // Artists usually don't have direct artwork in built-in MediaStore
                    albums = albums
                )
            }.sortedBy { it.name }
        }
    }

    override fun getArtistById(artistId: Long): Flow<Artist?> {
        // This is tricky because we generate IDs by hashCode. 
        // For now, we iterate all artists and find the matching ID.
        // In a real app with dedicated Artist table, this would be direct.
        return getAllArtists().map { artists ->
            artists.find { it.id == artistId }
        }
    }
    
    override fun searchArtists(query: String): Flow<List<Artist>> {
        return getAllArtists().map { artists ->
            artists.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    // ==================== Playlists ====================

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPlaylistById(id: Long): Flow<Playlist?> {
         return playlistDao.getPlaylistByIdFlow(id).map { it?.toDomain() }
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongs(playlistId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createPlaylist(name: String, description: String) {
        val entity = PlaylistEntity(name = name, description = description)
        playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    // ==================== Actions ====================

    override suspend fun getSongCount(): Int {
        return songDao.getSongCount()
    }
    
    override suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs.map { it.toEntity() })
    }
    
    override suspend fun deleteAllSongs() {
        songDao.deleteAllSongs()
    }
    
    override suspend fun incrementPlayCount(songId: Long) {
        songDao.incrementPlayCount(songId)
    }
    
    override suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean) {
        songDao.updateFavoriteStatus(songId, isFavorite)
    }
    
    override suspend fun scanMediaStore() {
        // TODO: Implement actual MediaStore scanning
        // For now, this is a placeholder to satisfy the interface
    }

    // ==================== Mappers ====================
    
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
    
    private fun PlaylistEntity.toDomain(): Playlist {
        return Playlist(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            songs = emptyList(), // Songs are loaded separately
            artworkUri = artworkUri,
            isSmartPlaylist = isSmartPlaylist
        )
    }
    
    private fun Playlist.toEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            artworkUri = artworkUri,
            isSmartPlaylist = isSmartPlaylist
        )
    }
}
