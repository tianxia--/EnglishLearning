package com.englishlearning.ui.screens.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages ExoPlayer for audio playback
 */
@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                DefaultDataSource.Factory(context)
            )
        )
        .build()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    var currentLessonId: String? = null
        private set

    // Position update handler
    private val positionUpdateHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val positionUpdateRunnable = object : Runnable {
        override fun run() {
            _currentPosition.value = exoPlayer.currentPosition
            _duration.value = exoPlayer.duration
            positionUpdateHandler.postDelayed(this, 100) // Update every 100ms
        }
    }

    init {
        exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _playbackState.value = when (playbackState) {
                    ExoPlayer.STATE_IDLE -> PlaybackState.Idle
                    ExoPlayer.STATE_BUFFERING -> PlaybackState.Buffering
                    ExoPlayer.STATE_READY -> {
                        // Update duration when player is ready
                        val newDuration = exoPlayer.duration
                        if (newDuration > 0) {
                            _duration.value = newDuration
                            android.util.Log.d("AudioManager", "Duration available: ${newDuration}ms (${newDuration/1000}s)")
                        }
                        if (exoPlayer.playWhenReady) PlaybackState.Playing
                        else PlaybackState.Paused
                    }
                    ExoPlayer.STATE_ENDED -> PlaybackState.Ended
                    else -> PlaybackState.Idle
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = if (isPlaying) PlaybackState.Playing else PlaybackState.Paused
            }
        })

        // Start position updates
        positionUpdateHandler.post(positionUpdateRunnable)
    }

    /**
     * Load audio file for a lesson
     */
    fun loadAudio(audioPath: String, lessonId: String) {
        currentLessonId = lessonId

        // Extract bookId from lessonId (e.g., "book1_lesson_001" -> "book1")
        val bookId = lessonId.substringBefore("_", "book1")

        // Build full asset path: bookId/filename
        val fullAssetPath = if (audioPath.contains("/")) {
            audioPath // Already contains path
        } else {
            "$bookId/$audioPath"
        }

        // Convert to assets URI format
        val assetUri = "file:///android_asset/$fullAssetPath"

        android.util.Log.d("AudioManager", "Loading audio: $audioPath -> $assetUri")
        val mediaItem = MediaItem.fromUri(assetUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        _duration.value = exoPlayer.duration
    }

    /**
     * Play audio
     */
    fun play() {
        exoPlayer.play()
    }

    /**
     * Pause audio
     */
    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Stop audio and reset
     */
    fun stop() {
        exoPlayer.stop()
        _currentPosition.value = 0L
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    /**
     * Skip forward by specified milliseconds
     */
    fun skipForward(milliseconds: Long = 10000L) {
        val newPosition = (exoPlayer.currentPosition + milliseconds).coerceAtMost(exoPlayer.duration)
        exoPlayer.seekTo(newPosition)
    }

    /**
     * Skip backward by specified milliseconds
     */
    fun skipBackward(milliseconds: Long = 10000L) {
        val newPosition = (exoPlayer.currentPosition - milliseconds).coerceAtLeast(0L)
        exoPlayer.seekTo(newPosition)
    }

    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer.setPlaybackSpeed(speed)
    }

    /**
     * Get current position in seconds
     */
    fun getCurrentPositionSeconds(): Double {
        return exoPlayer.currentPosition / 1000.0
    }

    /**
     * Get ExoPlayer instance (for MediaSession integration)
     */
    fun getExoPlayer(): ExoPlayer {
        return exoPlayer
    }

    /**
     * Release player resources
     */
    fun release() {
        positionUpdateHandler.removeCallbacks(positionUpdateRunnable)
        exoPlayer.release()
    }
}

/**
 * Playback state enum
 */
sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Ended : PlaybackState()
}
