package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricsState(
    val isLoading: Boolean = false,
    val lyrics: Lyrics? = null,
    val currentLineIndex: Int = 0,
    val error: String? = null,
    val source: LyricsSource = LyricsSource.NONE
)

@Serializable
data class Lyrics(
    val songId: Long,
    val title: String,
    val artist: String,
    val lines: List<LyricLine>,
    val isSynced: Boolean = false,
    val language: String = "unknown",
    val source: LyricsSource = LyricsSource.LOCAL
) {
    val plainText: String
        get() = lines.joinToString("\n") { it.text }
}

@Serializable
data class LyricLine(
    val startTime: Long, // milliseconds
    val endTime: Long = 0L,
    val text: String
) {
    val startTimeFormatted: String
        get() {
            val totalSeconds = startTime / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("[%02d:%02d]", minutes, seconds)
        }
}

@Serializable
enum class LyricsSource {
    NONE,
    LOCAL,      // من ملف .lrc محلي
    EMBEDDED,   // مضمنة في الملف الصوتي
    ONLINE      // من الإنترنت
}