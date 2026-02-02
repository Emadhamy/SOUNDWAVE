package com.soundwave.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["isFavorite"]),
        Index(value = ["playCount"]),
        Index(value = ["lastPlayed"])
    ]
)
data class SongEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val uri: String,
    val artworkUri: String?,
    val dateAdded: Long,
    val size: Long,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String = "",
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val rating: Int = 0, // 0-5
    val lyrics: String? = null,
    val comment: String? = null
)