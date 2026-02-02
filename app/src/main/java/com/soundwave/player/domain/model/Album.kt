package com.soundwave.player.domain.model

import android.net.Uri
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    @Contextual val artworkUri: Uri?,
    val songCount: Int,
    val year: Int = 0,
    val songs: List<Song> = emptyList()
) {
    val totalDuration: Long
        get() = songs.sumOf { it.duration }
        
    val totalDurationFormatted: String
        get() {
            val totalMinutes = totalDuration / 1000 / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "${hours}س ${minutes}د" else "${minutes} دقيقة"
        }
}