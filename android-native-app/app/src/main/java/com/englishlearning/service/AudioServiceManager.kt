package com.englishlearning.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

/**
 * Helper class to manage AudioPlayerService lifecycle
 */
class AudioServiceManager(private val context: Context) {

    private var service: AudioPlayerService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? AudioPlayerService.LocalBinder
            service = localBinder?.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    /**
     * Start the audio playback service
     */
    fun startService() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_START
        }
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Stop the audio playback service
     */
    fun stopService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_STOP
        }
        context.stopService(intent)
        service = null
    }

    /**
     * Get the service instance if bound
     */
    fun getService(): AudioPlayerService? = service

    /**
     * Check if service is bound
     */
    fun isServiceBound(): Boolean = isBound
}
