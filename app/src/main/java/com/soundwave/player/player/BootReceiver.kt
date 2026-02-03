package com.soundwave.player.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // يمكن إضافة منطق لاستعادة حالة التشغيل هنا
            // مثل استئناف قائمة التشغيل الأخيرة
        }
    }
}