package com.superbet.app

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.IBinder

class VolumeControlService : Service() {

    private var lastVolumeUpTime = 0L
    private var lastVolumeDownTime = 0L
    private var isActive = false
    private val DOUBLE_PRESS_INTERVAL = 500L

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                val volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)

                if (streamType == AudioManager.STREAM_MUSIC) {
                    handleVolumeChange(volume)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeReceiver, filter)
    }

    private fun handleVolumeChange(volume: Int) {
        val currentTime = System.currentTimeMillis()

        // Detect double press Volume UP (START) - complet stealth
        if (volume > 0) {
            if (currentTime - lastVolumeUpTime < DOUBLE_PRESS_INTERVAL) {
                if (!isActive) {
                    isActive = true
                    startTyping()
                }
                lastVolumeUpTime = 0
            } else {
                lastVolumeUpTime = currentTime
            }
        }

        // Detect double press Volume DOWN (STOP) - complet stealth
        if (volume == 0) {
            if (currentTime - lastVolumeDownTime < DOUBLE_PRESS_INTERVAL) {
                if (isActive) {
                    isActive = false
                    stopTyping()
                }
                lastVolumeDownTime = 0
            } else {
                lastVolumeDownTime = currentTime
            }
        }
    }

    private fun startTyping() {
        val intent = Intent(this, SuperbetAccessibilityService::class.java)
        intent.putExtra("action", "start")
        startService(intent)
        // FĂRĂ TOAST - complet stealth
    }

    private fun stopTyping() {
        val intent = Intent(this, SuperbetAccessibilityService::class.java)
        intent.putExtra("action", "stop")
        startService(intent)
        // FĂRĂ TOAST - complet stealth
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(volumeReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
