package com.soundwave.player.di

import android.content.Context
import androidx.room.Room
import com.soundwave.player.data.local.database.MusicDatabase
import com.soundwave.player.data.local.database.dao.LyricsDao
import com.soundwave.player.data.local.database.dao.PlaylistDao
import com.soundwave.player.data.local.database.dao.SettingsDao
import com.soundwave.player.data.local.database.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            MusicDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSongDao(database: MusicDatabase): SongDao {
        return database.songDao()
    }
    
    @Provides
    @Singleton
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao {
        return database.playlistDao()
    }
    
    @Provides
    @Singleton
    fun provideSettingsDao(database: MusicDatabase): SettingsDao {
        return database.settingsDao()
    }
    
    @Provides
    @Singleton
    fun provideLyricsDao(database: MusicDatabase): LyricsDao {
        return database.lyricsDao()
    }
}