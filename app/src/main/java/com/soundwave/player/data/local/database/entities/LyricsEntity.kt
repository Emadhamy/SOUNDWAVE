package com.soundwave.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lyrics",
    indices = [
        Index(value = ["songId"], unique = true)
    ]
)
data class LyricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val title: String,
    val artist: String,
    val lyricsJson: String, // JSON serialized LyricLines
    val isSynced: Boolean = false,
    val language: String = "unknown",
    val source: String = "LOCAL",
    val fetchedAt: Long = System.currentTimeMillis()
)