package com.soundwave.player.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.soundwave.player.data.local.database.dao.*
import com.soundwave.player.data.local.database.entities.*

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        QueueEntity::class,
        PlaybackStateEntity::class,
        EqualizerSettingsEntity::class,
        AppSettingsEntity::class,
        SleepTimerSettingsEntity::class,
        LyricsEntity::class
    ],
    version = 1,
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun settingsDao(): SettingsDao
    abstract fun lyricsDao(): LyricsDao
    
    companion object {
        const val DATABASE_NAME = "soundwave_database"
    }
}