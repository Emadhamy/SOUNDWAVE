package com.soundwave.player.player.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SleepTimerReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TIMER_FINISHED -> {
                // يتم التعامل معه في SleepTimerManager
            }
            SleepTimerManager.ACTION_CLOSE_APP -> {
                // إغلاق التطبيق
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }
    
    companion object {
        const val ACTION_TIMER_FINISHED = "com.soundwave.player.ACTION_TIMER_FINISHED"
    }
}