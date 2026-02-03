package com.soundwave.player.player.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MusicNotificationAdapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?
) : PlayerNotificationManager.MediaDescriptionAdapter {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val imageLoader = ImageLoader(context)
    
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.mediaMetadata.title ?: "بدون عنوان"
    }
    
    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return pendingIntent
    }
    
    override fun getCurrentContentText(player: Player): CharSequence? {
        return player.mediaMetadata.artist ?: "فنان غير معروف"
    }
    
    override fun getCurrentSubText(player: Player): CharSequence? {
        return player.mediaMetadata.albumTitle
    }
    
    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val artworkUri = player.mediaMetadata.artworkUri
        
        if (artworkUri != null) {
            scope.launch {
                val bitmap = loadBitmap(artworkUri.toString())
                bitmap?.let { callback.onBitmap(it) }
            }
        }
        
        return null
    }
    
    private suspend fun loadBitmap(uri: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .size(512, 512)
                .build()
            
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else null
        } catch (e: Exception) {
            null
        }
    }
}