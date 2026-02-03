package com.soundwave.player.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.soundwave.player.MainActivity
import com.soundwave.player.player.equalizer.EqualizerManager
import com.soundwave.player.player.notification.MusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    
    @Inject
    lateinit var player: ExoPlayer
    
    @Inject
    lateinit var notificationManager: MusicNotificationManager
    
    @Inject
    lateinit var equalizerManager: EqualizerManager
    
    private var mediaSession: MediaSession? = null
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    // تهيئة المعادل عند جاهزية المشغل
                    equalizerManager.initialize(player.audioSessionId)
                }
                Player.STATE_ENDED -> {
                    // يمكن إضافة منطق إضافي هنا
                }
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // تحديث حالة المعادل
            if (isPlaying) {
                equalizerManager.resume()
            }
        }
    }
    
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        // إعداد المشغل
        player.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
        
        // إعداد Intent للرجوع للتطبيق
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // إنشاء MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()
        
        // بدء الإشعار
        notificationManager.startNotification(mediaSession!!, this)
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.removeListener(playerListener)
            player.release()
            release()
        }
        equalizerManager.release()
        super.onDestroy()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }
    
    private inner class MediaSessionCallback : MediaSession.Callback {
        // يمكن إضافة callbacks مخصصة هنا
    }
}