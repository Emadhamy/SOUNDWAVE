package com.soundwave.player.player.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import com.soundwave.player.domain.model.EqualizerPreset
import com.soundwave.player.domain.model.EqualizerState
import com.soundwave.player.domain.model.ReverbPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor() {
    
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var presetReverb: PresetReverb? = null
    
    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()
    
    private val _bandFrequencies = MutableStateFlow<List<String>>(emptyList())
    val bandFrequencies: StateFlow<List<String>> = _bandFrequencies.asStateFlow()
    
    private val _numberOfBands = MutableStateFlow(10)
    val numberOfBands: StateFlow<Int> = _numberOfBands.asStateFlow()
    
    private var audioSessionId: Int = 0
    private var isInitialized = false
    
    fun initialize(sessionId: Int) {
        if (sessionId == 0) return
        if (sessionId == audioSessionId && isInitialized) return
        
        release()
        audioSessionId = sessionId
        
        try {
            // إنشاء المعادل
            equalizer = Equalizer(0, sessionId).apply {
                enabled = _state.value.isEnabled
            }
            
            equalizer?.let { eq ->
                _numberOfBands.value = eq.numberOfBands.toInt()
                
                // استخراج ترددات النطاقات
                val frequencies = (0 until eq.numberOfBands).map { band ->
                    val freq = eq.getCenterFreq(band.toShort()) / 1000
                    when {
                        freq >= 1000 -> "${freq / 1000}kHz"
                        else -> "${freq}Hz"
                    }
                }
                _bandFrequencies.value = frequencies
                
                // تطبيق المستويات المحفوظة
                applyBandLevels(eq, _state.value.bandLevels)
            }
            
            // Bass Boost
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = _state.value.isEnabled && _state.value.bassBoost > 0
                if (strengthSupported() && _state.value.bassBoost > 0) {
                    setStrength(_state.value.bassBoost.toShort())
                }
            }
            
            // Virtualizer
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = _state.value.isEnabled && _state.value.virtualizerStrength > 0
                if (strengthSupported() && _state.value.virtualizerStrength > 0) {
                    setStrength(_state.value.virtualizerStrength.toShort())
                }
            }
            
            // Loudness Enhancer
            try {
                loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                    enabled = _state.value.isEnabled && _state.value.loudnessGain > 0
                    if (_state.value.loudnessGain > 0) {
                        setTargetGain(_state.value.loudnessGain * 100)
                    }
                }
            } catch (e: Exception) {
                // قد لا يكون مدعوماً على بعض الأجهزة
            }
            
            // Reverb
            try {
                presetReverb = PresetReverb(0, sessionId).apply {
                    enabled = _state.value.isEnabled && _state.value.reverbPreset != ReverbPreset.NONE
                    if (_state.value.reverbPreset != ReverbPreset.NONE) {
                        preset = getReverbPresetValue(_state.value.reverbPreset)
                    }
                }
            } catch (e: Exception) {
                // قد لا يكون مدعوماً على بعض الأجهزة
            }
            
            isInitialized = true
            
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = false
        }
    }
    
    fun resume() {
        if (_state.value.isEnabled) {
            equalizer?.enabled = true
            bassBoost?.enabled = _state.value.bassBoost > 0
            virtualizer?.enabled = _state.value.virtualizerStrength > 0
            loudnessEnhancer?.enabled = _state.value.loudnessGain > 0
            presetReverb?.enabled = _state.value.reverbPreset != ReverbPreset.NONE
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled && _state.value.bassBoost > 0
        virtualizer?.enabled = enabled && _state.value.virtualizerStrength > 0
        loudnessEnhancer?.enabled = enabled && _state.value.loudnessGain > 0
        presetReverb?.enabled = enabled && _state.value.reverbPreset != ReverbPreset.NONE
        
        _state.update { it.copy(isEnabled = enabled) }
    }
    
    fun setBandLevel(bandIndex: Int, level: Int) {
        equalizer?.let { eq ->
            if (bandIndex < eq.numberOfBands) {
                val range = eq.bandLevelRange
                val minLevel = range[0]
                val maxLevel = range[1]
                // تحويل من -12..12 إلى نطاق المعادل الفعلي
                val scaledLevel = (minLevel + (level + 12) * (maxLevel - minLevel) / 24).toShort()
                eq.setBandLevel(bandIndex.toShort(), scaledLevel)
            }
        }
        
        val newLevels = _state.value.bandLevels.toMutableList()
        while (newLevels.size <= bandIndex) {
            newLevels.add(0)
        }
        newLevels[bandIndex] = level
        
        _state.update { 
            it.copy(
                bandLevels = newLevels,
                preset = EqualizerPreset.CUSTOM
            )
        }
    }
    
    fun setPreset(preset: EqualizerPreset) {
        equalizer?.let { eq ->
            applyBandLevels(eq, preset.levels)
        }
        _state.update { 
            it.copy(
                preset = preset, 
                bandLevels = preset.levels.toMutableList().apply {
                    while (size < _numberOfBands.value) add(0)
                }
            )
        }
    }
    
    fun setBassBoost(strength: Int) {
        val clampedStrength = strength.coerceIn(0, 1000)
        bassBoost?.apply {
            enabled = _state.value.isEnabled && clampedStrength > 0
            if (strengthSupported() && clampedStrength > 0) {
                setStrength(clampedStrength.toShort())
            }
        }
        _state.update { it.copy(bassBoost = clampedStrength) }
    }
    
    fun setVirtualizer(strength: Int) {
        val clampedStrength = strength.coerceIn(0, 1000)
        virtualizer?.apply {
            enabled = _state.value.isEnabled && clampedStrength > 0
            if (strengthSupported() && clampedStrength > 0) {
                setStrength(clampedStrength.toShort())
            }
        }
        _state.update { it.copy(virtualizerStrength = clampedStrength) }
    }
    
    fun setLoudnessGain(gain: Int) {
        val clampedGain = gain.coerceIn(0, 10)
        loudnessEnhancer?.apply {
            enabled = _state.value.isEnabled && clampedGain > 0
            if (clampedGain > 0) {
                setTargetGain(clampedGain * 100)
            }
        }
        _state.update { it.copy(loudnessGain = clampedGain) }
    }
    
    fun setReverb(preset: ReverbPreset) {
        presetReverb?.apply {
            enabled = _state.value.isEnabled && preset != ReverbPreset.NONE
            if (preset != ReverbPreset.NONE) {
                this.preset = getReverbPresetValue(preset)
            }
        }
        _state.update { it.copy(reverbPreset = preset) }
    }
    
    fun setStereoBalance(balance: Float) {
        // يتطلب تنفيذ مخصص
        _state.update { it.copy(stereoBalance = balance.coerceIn(-1f, 1f)) }
    }
    
    fun resetToDefault() {
        setPreset(EqualizerPreset.NORMAL)
        setBassBoost(0)
        setVirtualizer(0)
        setLoudnessGain(0)
        setReverb(ReverbPreset.NONE)
        setStereoBalance(0f)
    }
    
    fun loadState(state: EqualizerState) {
        _state.value = state
        if (isInitialized) {
            setEnabled(state.isEnabled)
            equalizer?.let { applyBandLevels(it, state.bandLevels) }
            setBassBoost(state.bassBoost)
            setVirtualizer(state.virtualizerStrength)
            setLoudnessGain(state.loudnessGain)
            setReverb(state.reverbPreset)
        }
    }
    
    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
            presetReverb?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        presetReverb = null
        isInitialized = false
    }
    
    private fun applyBandLevels(eq: Equalizer, levels: List<Int>) {
        val range = eq.bandLevelRange
        val minLevel = range[0]
        val maxLevel = range[1]
        
        levels.forEachIndexed { index, level ->
            if (index < eq.numberOfBands) {
                val scaledLevel = (minLevel + (level + 12) * (maxLevel - minLevel) / 24).toShort()
                eq.setBandLevel(index.toShort(), scaledLevel)
            }
        }
    }
    
    private fun getReverbPresetValue(preset: ReverbPreset): Short {
        return when (preset) {
            ReverbPreset.NONE -> PresetReverb.PRESET_NONE
            ReverbPreset.SMALL_ROOM -> PresetReverb.PRESET_SMALLROOM
            ReverbPreset.MEDIUM_ROOM -> PresetReverb.PRESET_MEDIUMROOM
            ReverbPreset.LARGE_ROOM -> PresetReverb.PRESET_LARGEROOM
            ReverbPreset.HALL -> PresetReverb.PRESET_MEDIUMHALL
            ReverbPreset.PLATE -> PresetReverb.PRESET_PLATE
            ReverbPreset.STUDIO -> PresetReverb.PRESET_LARGEHALL
            ReverbPreset.CHURCH -> PresetReverb.PRESET_LARGEHALL
        }
    }
    
    fun getBandLevelRange(): Pair<Int, Int> {
        return equalizer?.let { eq ->
            val range = eq.bandLevelRange
            Pair(range[0].toInt() / 100, range[1].toInt() / 100)
        } ?: Pair(-12, 12)
    }
    
    fun isSupported(): Boolean = isInitialized
    
    fun isBassBoostSupported(): Boolean = bassBoost?.strengthSupported() == true
    
    fun isVirtualizerSupported(): Boolean = virtualizer?.strengthSupported() == true
}