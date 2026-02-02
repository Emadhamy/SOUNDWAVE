package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VisualizerData(
    val waveform: ByteArray = ByteArray(0),
    val fft: ByteArray = ByteArray(0),
    val amplitudes: List<Float> = emptyList(),
    val frequencies: List<Float> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisualizerData

        if (!waveform.contentEquals(other.waveform)) return false
        if (!fft.contentEquals(other.fft)) return false
        if (amplitudes != other.amplitudes) return false
        if (frequencies != other.frequencies) return false

        return true
    }

    override fun hashCode(): Int {
        var result = waveform.contentHashCode()
        result = 31 * result + fft.contentHashCode()
        result = 31 * result + amplitudes.hashCode()
        result = 31 * result + frequencies.hashCode()
        return result
    }
}

@Serializable
enum class VisualizerStyle(val displayName: String) {
    BARS("أعمدة"),
    WAVE("موجات"),
    CIRCLE("دائري"),
    LINE("خطي"),
    BLOB("سائل"),
    PARTICLES("جسيمات"),
    SPECTRUM("طيف"),
    VINYL("فينيل"),
    NONE("بدون")
}

@Serializable
data class VisualizerSettings(
    val style: VisualizerStyle = VisualizerStyle.BARS,
    val sensitivity: Float = 1f,
    val smoothing: Float = 0.5f,
    val colorScheme: VisualizerColorScheme = VisualizerColorScheme.DEFAULT,
    val showOnLockScreen: Boolean = false
)

@Serializable
enum class VisualizerColorScheme(val displayName: String) {
    DEFAULT("افتراضي"),
    RAINBOW("قوس قزح"),
    MONOCHROME("أحادي"),
    ALBUM_BASED("حسب الألبوم"),
    FIRE("ناري"),
    OCEAN("بحري"),
    FOREST("غابة")
}