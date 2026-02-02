package com.soundwave.player.data.local.database.dao

import androidx.room.*
import com.soundwave.player.data.local.database.entities.PlaylistEntity
import com.soundwave.player.data.local.database.entities.PlaylistSongCrossRef
import com.soundwave.player.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    
    // ==================== القراءة ====================
    
    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistByIdFlow(id: Long): Flow<PlaylistEntity?>
    
    @Query("SELECT * FROM playlists WHERE isSmartPlaylist = 0 ORDER BY isPinned DESC, name COLLATE UNICODE ASC")
    fun getRegularPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE isSmartPlaylist = 1 ORDER BY name COLLATE UNICODE ASC")
    fun getSmartPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC
    """)
    fun getPlaylistSongs(playlistId: Long): Flow<List<SongEntity>>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC
    """)
    suspend fun getPlaylistSongsList(playlistId: Long): List<SongEntity>
    
    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getPlaylistSongCount(playlistId: Long): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
    
    @Query("SELECT playlistId FROM playlist_songs WHERE songId = :songId")
    suspend fun getPlaylistsContainingSong(songId: Long): List<Long>
    
    // ==================== الكتابة ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Query("UPDATE playlists SET name = :name, description = :description, updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistInfo(playlistId: Long, name: String, description: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE playlists SET isPinned = :isPinned WHERE id = :playlistId")
    suspend fun updatePinnedStatus(playlistId: Long, isPinned: Boolean)
    
    @Query("UPDATE playlists SET artworkUri = :artworkUri WHERE id = :playlistId")
    suspend fun updateArtwork(playlistId: Long, artworkUri: String?)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)
    
    // ==================== أغاني القائمة ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongsToPlaylist(crossRefs: List<PlaylistSongCrossRef>)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
    
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int
    
    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE playlist_songs 
        SET position = :newPosition 
        WHERE playlistId = :playlistId AND songId = :songId
    """)
    suspend fun updateSongPosition(playlistId: Long, songId: Long, newPosition: Int)
    
    // ==================== الإحصائيات ====================
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
}