package com.soundwave.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "queue",
    indices = [Index(value = ["position"])]
)
data class QueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentSongId: Long? = null,
    val currentPosition: Long = 0,
    val repeatMode: String = "OFF",
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val lastUpdated: Long = System.currentTimeMillis()
)