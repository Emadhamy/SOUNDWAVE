package com.soundwave.player.data.local.database.dao

import androidx.room.*
import com.soundwave.player.data.local.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    
    // ==================== Equalizer Settings ====================
    
    @Query("SELECT * FROM equalizer_settings WHERE id = 1")
    fun getEqualizerSettings(): Flow<EqualizerSettingsEntity?>
    
    @Query("SELECT * FROM equalizer_settings WHERE id = 1")
    suspend fun getEqualizerSettingsSync(): EqualizerSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEqualizerSettings(settings: EqualizerSettingsEntity)
    
    // ==================== App Settings ====================
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettingsSync(): AppSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppSettings(settings: AppSettingsEntity)
    
    // ==================== Playback State ====================
    
    @Query("SELECT * FROM playback_state WHERE id = 1")
    suspend fun getPlaybackState(): PlaybackStateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaybackState(state: PlaybackStateEntity)
    
    // ==================== Sleep Timer Settings ====================
    
    @Query("SELECT * FROM sleep_timer_settings WHERE id = 1")
    fun getSleepTimerSettings(): Flow<SleepTimerSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSleepTimerSettings(settings: SleepTimerSettingsEntity)
    
    // ==================== Queue ====================
    
    @Query("SELECT songId FROM queue ORDER BY position ASC")
    suspend fun getQueueSongIds(): List<Long>
    
    @Query("DELETE FROM queue")
    suspend fun clearQueue()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueEntity>)
}