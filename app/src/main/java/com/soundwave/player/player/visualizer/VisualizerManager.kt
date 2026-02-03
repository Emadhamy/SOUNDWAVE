package com.soundwave.player.player.visualizer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import com.soundwave.player.domain.model.VisualizerData
import com.soundwave.player.domain.model.VisualizerSettings
import com.soundwave.player.domain.model.VisualizerStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.hypot

@Singleton
class VisualizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var visualizer: Visualizer? = null
    
    private val _data = MutableStateFlow(VisualizerData())
    val data: StateFlow<VisualizerData> = _data.asStateFlow()
    
    private val _settings = MutableStateFlow(VisualizerSettings())
    val settings: StateFlow<VisualizerSettings> = _settings.asStateFlow()
    
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private var audioSessionId: Int = 0
    
    fun initialize(sessionId: Int) {
        if (!hasRecordPermission()) return
        if (sessionId == audioSessionId && visualizer != null) return
        
        release()
        audioSessionId = sessionId
        
        try {
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { processWaveform(it) }
                        }
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { processFft(it) }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        if (!hasRecordPermission()) return
        
        try {
            visualizer?.enabled = enabled
            _isEnabled.value = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setStyle(style: VisualizerStyle) {
        _settings.update { it.copy(style = style) }
    }
    
    fun setSensitivity(sensitivity: Float) {
        _settings.update { it.copy(sensitivity = sensitivity.coerceIn(0.1f, 3f)) }
    }
    
    fun setSmoothing(smoothing: Float) {
        _settings.update { it.copy(smoothing = smoothing.coerceIn(0f, 1f)) }
    }
    
    fun updateSettings(newSettings: VisualizerSettings) {
        _settings.value = newSettings
    }
    
    private fun processWaveform(waveform: ByteArray) {
        val sensitivity = _settings.value.sensitivity
        
        // تحويل إلى amplitudes
        val amplitudes = waveform.map { byte ->
            (abs(byte.toInt() - 128) / 128f) * sensitivity
        }
        
        _data.update { 
            it.copy(
                waveform = waveform,
                amplitudes = amplitudes
            )
        }
    }
    
    private fun processFft(fft: ByteArray) {
        val sensitivity = _settings.value.sensitivity
        val n = fft.size / 2
        
        // حساب magnitudes من FFT
        val frequencies = mutableListOf<Float>()
        for (i in 0 until n step 2) {
            val real = fft[i].toFloat()
            val imaginary = fft[i + 1].toFloat()
            val magnitude = hypot(real, imaginary) / 256f * sensitivity
            frequencies.add(magnitude.coerceIn(0f, 1f))
        }
        
        _data.update { 
            it.copy(
                fft = fft,
                frequencies = frequencies
            )
        }
    }
    
    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        visualizer = null
        _isEnabled.value = false
    }
    
    private fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun isSupported(): Boolean = hasRecordPermission()
}