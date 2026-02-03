package com.soundwave.player.player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.soundwave.player.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "soundwave_playback_channel"
        const val NOTIFICATION_ID = 1001
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private var playerNotificationManager: PlayerNotificationManager? = null
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "تشغيل الموسيقى",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار تشغيل الموسيقى"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    @OptIn(UnstableApi::class)
    fun startNotification(
        mediaSession: MediaSession,
        service: MediaSessionService
    ) {
        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(
                MusicNotificationAdapter(context, mediaSession.sessionActivity)
            )
            .setSmallIconResourceId(R.drawable.ic_music_note)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .build()
            .apply {
                setMediaSessionToken(mediaSession.sessionCompatToken)
                setUseFastForwardActionInCompactView(false)
                setUseRewindActionInCompactView(false)
                setUseNextActionInCompactView(true)
                setUsePreviousActionInCompactView(true)
                setUsePlayPauseActions(true)
                setPlayer(mediaSession.player)
            }
    }
    
    fun stopNotification() {
        playerNotificationManager?.setPlayer(null)
        notificationManager.cancel(NOTIFICATION_ID)
    }
}