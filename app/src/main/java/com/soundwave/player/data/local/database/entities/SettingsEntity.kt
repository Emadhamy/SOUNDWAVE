package com.soundwave.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equalizer_settings")
data class EqualizerSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val isEnabled: Boolean = false,
    val presetName: String = "NORMAL",
    val bandLevels: String = "0,0,0,0,0,0,0,0,0,0",
    val bassBoost: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0,
    val reverbPreset: String = "NONE",
    val stereoBalance: Float = 0f
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: String = "SYSTEM",
    val dynamicColors: Boolean = true,
    val language: String = "ar",
    val gaplessPlayback: Boolean = true,
    val crossfadeDuration: Int = 0,
    val replayGain: Boolean = false,
    val filterShortSongs: Boolean = true,
    val minSongDuration: Int = 30,
    val showAlbumArtOnLockScreen: Boolean = true,
    val keepScreenOn: Boolean = false,
    val autoPlay: Boolean = false,
    val visualizerStyle: String = "BARS",
    val visualizerSensitivity: Float = 1f
)

@Entity(tableName = "sleep_timer_settings")
data class SleepTimerSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val lastDurationMinutes: Int = 30,
    val action: String = "PAUSE",
    val fadeOutEnabled: Boolean = true,
    val fadeOutDuration: Long = 30000,
    val finishCurrentSong: Boolean = true
)