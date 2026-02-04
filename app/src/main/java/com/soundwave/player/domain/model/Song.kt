package com.soundwave.player.domain.model

import android.net.Uri
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    @Contextual val uri: Uri,
    @Contextual val artworkUri: Uri?,
    val dateAdded: Long,
    val size: Long,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String = "",
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0
) {
    val durationFormatted: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    
    val durationFormattedLong: String
        get() {
            val totalSeconds = duration / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
    
    val sizeFormatted: String
        get() {
            val mb = size / (1024.0 * 1024.0)
            return String.format("%.2f MB", mb)
        }
        
    val folder: String
        get() = path.substringBeforeLast("/")
}