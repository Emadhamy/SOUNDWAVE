package com.soundwave.player.data.local.database.dao

import androidx.room.*
import com.soundwave.player.data.local.database.entities.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    
    @Query("SELECT * FROM lyrics WHERE songId = :songId")
    fun getLyricsBySongId(songId: Long): Flow<LyricsEntity?>
    
    @Query("SELECT * FROM lyrics WHERE songId = :songId")
    suspend fun getLyricsBySongIdSync(songId: Long): LyricsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)
    
    @Query("DELETE FROM lyrics WHERE songId = :songId")
    suspend fun deleteLyrics(songId: Long)
    
    @Query("DELETE FROM lyrics")
    suspend fun deleteAllLyrics()
    
    @Query("SELECT COUNT(*) FROM lyrics")
    suspend fun getLyricsCount(): Int
    
    @Query("SELECT songId FROM lyrics")
    suspend fun getSongsWithLyrics(): List<Long>
}