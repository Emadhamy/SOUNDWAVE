package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingTimeMs: Long = 0L,
    val endTime: Long = 0L,
    val action: SleepTimerAction = SleepTimerAction.PAUSE,
    val fadeOutEnabled: Boolean = true,
    val fadeOutDuration: Long = 30_000L, // 30 ثانية
    val finishCurrentSong: Boolean = true
) {
    val remainingMinutes: Int
        get() = (remainingTimeMs / 1000 / 60).toInt()
        
    val remainingSeconds: Int
        get() = ((remainingTimeMs / 1000) % 60).toInt()
        
    val remainingFormatted: String
        get() {
            val hours = remainingTimeMs / 1000 / 3600
            val minutes = (remainingTimeMs / 1000 % 3600) / 60
            val seconds = remainingTimeMs / 1000 % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

@Serializable
enum class SleepTimerAction {
    PAUSE,          // إيقاف مؤقت
    STOP,           // إيقاف كامل
    CLOSE_APP       // إغلاق التطبيق
}

@Serializable
data class SleepTimerPreset(
    val name: String,
    val durationMinutes: Int,
    val icon: String = "⏰"
) {
    companion object {
        val presets = listOf(
            SleepTimerPreset("15 دقيقة", 15, "🕐"),
            SleepTimerPreset("30 دقيقة", 30, "🕐"),
            SleepTimerPreset("45 دقيقة", 45, "🕐"),
            SleepTimerPreset("ساعة", 60, "🕐"),
            SleepTimerPreset("ساعة ونصف", 90, "🕐"),
            SleepTimerPreset("ساعتين", 120, "🕐")
        )
    }
}