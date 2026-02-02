package com.soundwave.player.domain.model

import android.net.Uri
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    @Contextual val artworkUri: Uri? = null,
    val albums: List<Album> = emptyList()
) {
    val totalSongs: Int
        get() = albums.sumOf { it.songCount }
}