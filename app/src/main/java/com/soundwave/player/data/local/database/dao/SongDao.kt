package com.soundwave.player.data.local.database.dao

import androidx.room.*
import com.soundwave.player.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    
    // ==================== القراءة ====================
    
    @Query("SELECT * FROM songs ORDER BY title COLLATE UNICODE ASC")
    fun getAllSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY title COLLATE UNICODE ASC")
    suspend fun getAllSongsList(): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?
    
    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongByIdFlow(id: Long): Flow<SongEntity?>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title COLLATE UNICODE ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedSongs(limit: Int = 50): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE playCount = 0 ORDER BY RANDOM() LIMIT :limit")
    fun getNeverPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    
    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN artist LIKE :query || '%' THEN 2
                WHEN album LIKE :query || '%' THEN 3
                ELSE 4
            END,
            title COLLATE UNICODE ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album, trackNumber")
    fun getSongsByArtist(artist: String): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber, title")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE genre = :genre ORDER BY title COLLATE UNICODE ASC")
    fun getSongsByGenre(genre: String): Flow<List<SongEntity>>
    
    @Query("SELECT DISTINCT genre FROM songs WHERE genre != '' ORDER BY genre COLLATE UNICODE ASC")
    fun getAllGenres(): Flow<List<String>>
    
    @Query("SELECT DISTINCT path FROM songs")
    suspend fun getAllPaths(): List<String>
    
    @Query("SELECT * FROM songs WHERE path LIKE :folderPath || '%' ORDER BY title")
    fun getSongsByFolder(folderPath: String): Flow<List<SongEntity>>
    
    @Query("SELECT DISTINCT SUBSTR(path, 1, LENGTH(path) - LENGTH(SUBSTR(path, -INSTR(REVERSE(path), '/')))) as folder FROM songs ORDER BY folder")
    fun getAllFolders(): Flow<List<String>>
    
    // ==================== الكتابة ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Update
    suspend fun updateSong(song: SongEntity)
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE songs SET rating = :rating WHERE id = :songId")
    suspend fun updateRating(songId: Long, rating: Int)
    
    @Query("UPDATE songs SET lyrics = :lyrics WHERE id = :songId")
    suspend fun updateLyrics(songId: Long, lyrics: String?)
    
    @Delete
    suspend fun deleteSong(song: SongEntity)
    
    @Query("DELETE FROM songs WHERE id NOT IN (:validIds)")
    suspend fun deleteInvalidSongs(validIds: List<Long>)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    // ==================== الإحصائيات ====================
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("SELECT COUNT(*) FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int
    
    @Query("SELECT SUM(duration) FROM songs")
    suspend fun getTotalDuration(): Long?
    
    @Query("SELECT SUM(playCount) FROM songs")
    suspend fun getTotalPlayCount(): Int?
    
    @Query("SELECT artist, COUNT(*) as count FROM songs GROUP BY artist ORDER BY count DESC LIMIT :limit")
    suspend fun getTopArtists(limit: Int = 10): List<ArtistCount>
    
    @Query("SELECT genre, COUNT(*) as count FROM songs WHERE genre != '' GROUP BY genre ORDER BY count DESC LIMIT :limit")
    suspend fun getTopGenres(limit: Int = 10): List<GenreCount>
}

data class ArtistCount(
    val artist: String,
    val count: Int
)

data class GenreCount(
    val genre: String,
    val count: Int
)