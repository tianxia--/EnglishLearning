package com.englishlearning.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.englishlearning.ui.screens.player.AudioManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service for audio playback with MediaSession support
 * Provides background playback and lock screen controls
 */
@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var audioManager: AudioManager

    private var mediaSession: MediaSession? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // Service will be kept alive by MediaSession
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }
        return START_STICKY
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    private fun initializeMediaSession() {
        if (mediaSession != null) return

        // Create MediaSession with ExoPlayer from AudioManager
        // MediaSession will automatically handle playback controls through ExoPlayer
        mediaSession = MediaSession.Builder(this, audioManager.getExoPlayer())
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback controls"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't release the player here as AudioManager manages it
        mediaSession?.release()
        mediaSession = null
    }

    companion object {
        const val ACTION_START = "com.englishlearning.service.START"
        const val ACTION_STOP = "com.englishlearning.service.STOP"
        const val CHANNEL_ID = "audio_playback_channel"
    }
}
