package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val songs: List<Song> = emptyList(),
    val artworkUri: String? = null,
    val isSmartPlaylist: Boolean = false,
    val smartCriteria: SmartPlaylistCriteria? = null
) {
    val songCount: Int get() = songs.size
    
    val totalDuration: Long get() = songs.sumOf { it.duration }
    
    val totalDurationFormatted: String
        get() {
            val totalMinutes = totalDuration / 1000 / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "${hours}س ${minutes}د" else "${minutes} دقيقة"
        }
}

@Serializable
data class SmartPlaylistCriteria(
    val type: SmartPlaylistType,
    val limit: Int = 50,
    val sortBy: SortBy = SortBy.DATE_ADDED,
    val sortOrder: SortOrder = SortOrder.DESCENDING
)

@Serializable
enum class SmartPlaylistType {
    RECENTLY_PLAYED,
    MOST_PLAYED,
    RECENTLY_ADDED,
    FAVORITES,
    NEVER_PLAYED
}

@Serializable
enum class SortBy {
    TITLE, ARTIST, ALBUM, DATE_ADDED, PLAY_COUNT, DURATION
}

@Serializable
enum class SortOrder {
    ASCENDING, DESCENDING
}