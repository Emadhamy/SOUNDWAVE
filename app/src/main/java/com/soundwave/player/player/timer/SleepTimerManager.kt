package com.soundwave.player.player.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import com.soundwave.player.domain.model.SleepTimerAction
import com.soundwave.player.domain.model.SleepTimerState
import com.soundwave.player.player.MusicPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepTimerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicPlayer: MusicPlayer
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()
    
    private var countDownTimer: CountDownTimer? = null
    private var fadeOutJob: Job? = null
    
    fun setTimer(durationMinutes: Int) {
        cancel()
        
        val durationMs = durationMinutes * 60 * 1000L
        val endTime = System.currentTimeMillis() + durationMs
        
        _state.update { 
            it.copy(
                isActive = true,
                remainingTimeMs = durationMs,
                endTime = endTime
            )
        }
        
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _state.update { it.copy(remainingTimeMs = millisUntilFinished) }
                
                // بدء التلاشي التدريجي إذا كان مفعلاً
                if (_state.value.fadeOutEnabled && 
                    millisUntilFinished <= _state.value.fadeOutDuration &&
                    fadeOutJob == null) {
                    startFadeOut(millisUntilFinished)
                }
            }
            
            override fun onFinish() {
                executeTimerAction()
            }
        }.start()
    }
    
    fun setTimerWithOptions(
        durationMinutes: Int,
        action: SleepTimerAction = SleepTimerAction.PAUSE,
        fadeOutEnabled: Boolean = true,
        fadeOutDuration: Long = 30_000L,
        finishCurrentSong: Boolean = true
    ) {
        _state.update { 
            it.copy(
                action = action,
                fadeOutEnabled = fadeOutEnabled,
                fadeOutDuration = fadeOutDuration,
                finishCurrentSong = finishCurrentSong
            )
        }
        setTimer(durationMinutes)
    }
    
    fun extendTimer(additionalMinutes: Int) {
        if (!_state.value.isActive) return
        
        val additionalMs = additionalMinutes * 60 * 1000L
        val newDuration = _state.value.remainingTimeMs + additionalMs
        
        cancel()
        setTimer((newDuration / 60000).toInt())
    }
    
    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
        fadeOutJob?.cancel()
        fadeOutJob = null
        
        // إعادة الصوت للمستوى الطبيعي
        musicPlayer.setVolume(1f)
        
        _state.update { 
            it.copy(
                isActive = false,
                remainingTimeMs = 0L,
                endTime = 0L
            )
        }
    }
    
    private fun startFadeOut(remainingMs: Long) {
        fadeOutJob = scope.launch {
            val startVolume = 1f
            val steps = 30
            val stepDuration = remainingMs / steps
            
            for (i in steps downTo 0) {
                val volume = (i.toFloat() / steps) * startVolume
                musicPlayer.setVolume(volume)
                delay(stepDuration)
            }
        }
    }
    
    private fun executeTimerAction() {
        fadeOutJob?.cancel()
        
        when (_state.value.action) {
            SleepTimerAction.PAUSE -> {
                if (_state.value.finishCurrentSong) {
                    // انتظر نهاية الأغنية الحالية
                    scope.launch {
                        waitForSongEnd()
                        musicPlayer.pause()
                        resetState()
                    }
                } else {
                    musicPlayer.pause()
                    resetState()
                }
            }
            SleepTimerAction.STOP -> {
                musicPlayer.stop()
                resetState()
            }
            SleepTimerAction.CLOSE_APP -> {
                musicPlayer.stop()
                // إرسال broadcast لإغلاق التطبيق
                context.sendBroadcast(Intent(ACTION_CLOSE_APP))
                resetState()
            }
        }
    }
    
    private suspend fun waitForSongEnd() {
        val playerState = musicPlayer.playerState.value
        val remaining = playerState.duration - playerState.currentPosition
        if (remaining > 0) {
            delay(remaining)
        }
    }
    
    private fun resetState() {
        musicPlayer.setVolume(1f)
        _state.update { 
            it.copy(
                isActive = false,
                remainingTimeMs = 0L,
                endTime = 0L
            )
        }
    }
    
    companion object {
        const val ACTION_CLOSE_APP = "com.soundwave.player.ACTION_CLOSE_APP"
    }
}