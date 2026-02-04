package com.soundwave.player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EqualizerState(
    val isEnabled: Boolean = false,
    val isSmartEnhanceEnabled: Boolean = false,
    val preset: EqualizerPreset = EqualizerPreset.NORMAL,
    val bandLevels: List<Int> = List(10) { 0 },
    val bassBoost: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0,
    val reverbPreset: ReverbPreset = ReverbPreset.NONE,
    val stereoBalance: Float = 0f // -1 = left, 0 = center, 1 = right
)

@Serializable
enum class EqualizerPreset(val displayName: String, val levels: List<Int>) {
    NORMAL("عادي", listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
    POP("بوب", listOf(1, 3, 4, 3, 0, -1, -1, 0, 1, 2)),
    ROCK("روك", listOf(4, 3, 1, 0, -1, 0, 1, 2, 3, 4)),
    JAZZ("جاز", listOf(3, 2, 1, 2, -1, -1, 0, 1, 2, 3)),
    CLASSICAL("كلاسيكي", listOf(4, 3, 2, 1, 0, 0, 0, 1, 2, 3)),
    HIPHOP("هيب هوب", listOf(4, 3, 1, 2, -1, 0, 1, 0, 2, 3)),
    ELECTRONIC("إلكتروني", listOf(3, 4, 2, 0, -2, 0, 1, 3, 4, 3)),
    RNB("R&B", listOf(2, 5, 4, 1, -1, -1, 1, 2, 2, 3)),
    VOCAL("صوتي", listOf(-1, 0, 2, 3, 3, 2, 1, 0, -1, -2)),
    BASS_BOOST("تعزيز الباس", listOf(5, 4, 3, 2, 1, 0, 0, 0, 0, 0)),
    TREBLE_BOOST("تعزيز التريبل", listOf(0, 0, 0, 0, 0, 1, 2, 3, 4, 5)),
    LOUDNESS("صوت عالي", listOf(4, 3, 0, 0, -1, -1, 0, 0, 3, 4)),
    SPOKEN_WORD("كلام", listOf(-1, 0, 0, 2, 3, 3, 2, 0, 0, -1)),
    ACOUSTIC("أكوستيك", listOf(3, 2, 1, 1, 2, 1, 2, 2, 2, 3)),
    CRYSTAL_CLEAR("وضوح فائق", listOf(2, 2, 1, 0, 1, 2, 3, 4, 3, 2)),
    VOCAL_BOOST("تعزيز الصوت", listOf(-1, 0, 1, 3, 4, 3, 2, 1, 0, -1)),
    DEEP_BASS("باس عميق", listOf(6, 5, 3, 1, 0, 0, 0, 0, 1, 1)),
    CUSTOM("مخصص", listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    
    companion object {
        fun fromName(name: String): EqualizerPreset {
            return entries.find { it.name == name } ?: NORMAL
        }
    }
}

@Serializable
enum class ReverbPreset(val displayName: String) {
    NONE("بدون"),
    SMALL_ROOM("غرفة صغيرة"),
    MEDIUM_ROOM("غرفة متوسطة"),
    LARGE_ROOM("غرفة كبيرة"),
    HALL("قاعة"),
    PLATE("معدني"),
    STUDIO("ستوديو"),
    CHURCH("كنيسة")
}