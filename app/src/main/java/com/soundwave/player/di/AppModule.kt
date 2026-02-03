package com.soundwave.player.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.soundwave.player.player.MusicPlayer
import com.soundwave.player.player.equalizer.EqualizerManager
import com.soundwave.player.player.lyrics.LyricsManager
import com.soundwave.player.player.timer.SleepTimerManager
import com.soundwave.player.player.visualizer.VisualizerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }
    }
}