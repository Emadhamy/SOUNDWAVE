package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val currentSong: Song? = null,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val volume: Float = 1f,
    val isBuffering: Boolean = false,
    val error: String? = null
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
    
    val progressPercent: Int
        get() = (progress * 100).toInt()
    
    val hasNext: Boolean
        get() = currentIndex < playlist.lastIndex || repeatMode == RepeatMode.ALL
    
    val hasPrevious: Boolean
        get() = currentIndex > 0 || repeatMode == RepeatMode.ALL
        
    val remainingTime: Long
        get() = duration - currentPosition
        
    val remainingTimeFormatted: String
        get() {
            val totalSeconds = remainingTime / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("-%d:%02d", minutes, seconds)
        }
}

@Serializable
enum class RepeatMode {
    OFF,
    ONE,
    ALL;
    
    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}