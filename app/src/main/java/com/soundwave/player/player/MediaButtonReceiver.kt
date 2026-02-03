package com.soundwave.player.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.media3.session.MediaButtonReceiver as Media3ButtonReceiver

class MediaButtonReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY,
                    KeyEvent.KEYCODE_MEDIA_PAUSE,
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        // تمرير للـ MediaSessionService
                        Media3ButtonReceiver.onReceive(context, intent)
                    }
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        Media3ButtonReceiver.onReceive(context, intent)
                    }
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        Media3ButtonReceiver.onReceive(context, intent)
                    }
                    KeyEvent.KEYCODE_MEDIA_STOP -> {
                        Media3ButtonReceiver.onReceive(context, intent)
                    }
                }
            }
        }
    }
}