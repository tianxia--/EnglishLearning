package com.englishlearning.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Lesson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global playback state manager
 * Maintains the current playing lesson and playback state across the app
 */
@Singleton
class PlaybackStateManager @Inject constructor(
    private val audioManager: AudioManager
) {

    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _bookId = MutableStateFlow("book1")
    val bookId: StateFlow<String> = _bookId.asStateFlow()

    private val _lessonId = MutableStateFlow<String?>(null)
    val lessonId: StateFlow<String?> = _lessonId.asStateFlow()

    init {
        // Observe AudioManager's playback state to keep in sync
        syncWithAudioManager()
    }

    /**
     * Sync playing state with AudioManager
     */
    private fun syncWithAudioManager() {
        // Since we're not in a ViewModel/CoroutineScope context, we need to handle this differently
        // We'll update the state whenever setPlaying is called, or expose a method to sync
        android.util.Log.d("PlaybackStateManager", "Initialized with AudioManager")
    }

    /**
     * Set the current playing lesson
     */
    fun setCurrentLesson(bookId: String, lesson: Lesson) {
        _bookId.value = bookId
        _currentLesson.value = lesson
        _lessonId.value = lesson.id
        android.util.Log.d("PlaybackStateManager", "Current lesson set: ${lesson.title} ($bookId/${lesson.id})")
    }

    /**
     * Update playback state (call this when AudioManager state changes)
     */
    fun setPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        android.util.Log.d("PlaybackStateManager", "Playing state: $isPlaying")
    }

    /**
     * Sync with actual AudioManager state (call this periodically or when needed)
     */
    fun syncState() {
        val actualState = audioManager.playbackState.value
        val isActuallyPlaying = actualState is PlaybackState.Playing
        _isPlaying.value = isActuallyPlaying
        android.util.Log.d("PlaybackStateManager", "Synced state: $isActuallyPlaying (from ${actualState::class.simpleName})")
    }

    /**
     * Clear current lesson (when playback is stopped/completed)
     */
    fun clearCurrentLesson() {
        _currentLesson.value = null
        _lessonId.value = null
        _isPlaying.value = false
        android.util.Log.d("PlaybackStateManager", "Current lesson cleared")
    }

    /**
     * Check if there's an active lesson
     */
    fun hasActiveLesson(): Boolean {
        return _currentLesson.value != null
    }

    /**
     * Get current lesson ID
     */
    fun getCurrentLessonId(): String? {
        return _lessonId.value
    }

    /**
     * Get current book ID
     */
    fun getCurrentBookId(): String {
        return _bookId.value
    }
}
