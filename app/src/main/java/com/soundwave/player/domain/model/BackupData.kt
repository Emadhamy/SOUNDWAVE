package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val playlists: List<PlaylistBackup> = emptyList(),
    val favorites: List<Long> = emptyList(), // song IDs
    val playCounts: Map<Long, Int> = emptyMap(),
    val equalizerSettings: EqualizerState? = null,
    val appSettings: AppSettingsBackup? = null,
    val statistics: StatisticsBackup? = null
)

@Serializable
data class PlaylistBackup(
    val name: String,
    val description: String,
    val songPaths: List<String>, // نحفظ المسارات بدلاً من IDs
    val createdAt: Long,
    val isSmartPlaylist: Boolean,
    val smartCriteria: SmartPlaylistCriteria?
)

@Serializable
data class AppSettingsBackup(
    val themeMode: String,
    val dynamicColors: Boolean,
    val language: String,
    val gaplessPlayback: Boolean,
    val crossfadeDuration: Int,
    val replayGain: Boolean,
    val sleepTimerDefaults: SleepTimerState?
)

@Serializable
data class StatisticsBackup(
    val totalPlayTime: Long,
    val totalSongsPlayed: Int,
    val topArtists: List<String>,
    val topSongs: List<String>,
    val listeningHistory: List<ListeningSession>
)

@Serializable
data class ListeningSession(
    val date: Long,
    val durationMs: Long,
    val songsPlayed: Int
)