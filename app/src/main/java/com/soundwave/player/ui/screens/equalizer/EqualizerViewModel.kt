package com.soundwave.player.ui.screens.equalizer

import androidx.lifecycle.ViewModel
import com.soundwave.player.domain.model.EqualizerPreset
import com.soundwave.player.domain.model.EqualizerState
import com.soundwave.player.domain.model.ReverbPreset
import com.soundwave.player.player.equalizer.EqualizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager
) : ViewModel() {
    
    val state: StateFlow<EqualizerState> = equalizerManager.state
    val bandFrequencies: StateFlow<List<String>> = equalizerManager.bandFrequencies
    val numberOfBands: StateFlow<Int> = equalizerManager.numberOfBands
    
    fun setEnabled(enabled: Boolean) {
        equalizerManager.setEnabled(enabled)
    }
    
    fun setBandLevel(bandIndex: Int, level: Int) {
        equalizerManager.setBandLevel(bandIndex, level)
    }
    
    fun setPreset(preset: EqualizerPreset) {
        equalizerManager.setPreset(preset)
    }
    
    fun setBassBoost(strength: Int) {
        equalizerManager.setBassBoost(strength)
    }
    
    fun setVirtualizer(strength: Int) {
        equalizerManager.setVirtualizer(strength)
    }
    
    fun setLoudnessGain(gain: Int) {
        equalizerManager.setLoudnessGain(gain)
    }
    
    fun setReverb(preset: ReverbPreset) {
        equalizerManager.setReverb(preset)
    }
    
    fun setSmartEnhancementEnabled(enabled: Boolean) {
        equalizerManager.setSmartEnhancementEnabled(enabled)
    }
    
    fun resetToDefault() {
        equalizerManager.resetToDefault()
    }
    
    fun isSupported(): Boolean = equalizerManager.isSupported()
}