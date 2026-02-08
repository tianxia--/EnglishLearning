package com.englishlearning.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.model.LessonProgress
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.repository.LessonRepository
import com.englishlearning.data.repository.AIRepository
import com.englishlearning.data.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the audio player screen - 使用统一的设置管理器
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val lessonRepository: LessonRepository,
    private val audioManager: AudioManager,
    private val lrcSyncManager: LrcSyncManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val playbackStateManager: PlaybackStateManager,
    private val aiRepository: AIRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>("bookId") ?: "book1"
    private val initialLessonId: String = savedStateHandle.get<String>("lessonId") ?: "book1_lesson_001"
    private var currentLessonId: String = initialLessonId
        private set

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _currentPosition = audioManager.currentPosition
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = audioManager.duration
    val duration: StateFlow<Long> = _duration

    private val _playbackState = audioManager.playbackState
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _playbackSpeed = audioManager.playbackSpeed
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _currentSegmentIndex = lrcSyncManager.currentSegmentIndex
    val currentSegmentIndex: StateFlow<Int> = _currentSegmentIndex

    // 从统一的设置管理器获取设置
    val isTranscriptVisible: StateFlow<Boolean> = userPreferencesManager.userPreferences
        .map { it.showTranscriptByDefault }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val autoPlayNext: StateFlow<Boolean> = userPreferencesManager.userPreferences
        .map { it.autoPlayNext }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // AI Explanation State
    private val _aiExplanationState = MutableStateFlow<AiExplanationState>(AiExplanationState.Idle)
    val aiExplanationState: StateFlow<AiExplanationState> = _aiExplanationState.asStateFlow()

    fun explainSentence(sentence: String) {
        viewModelScope.launch {
            _aiExplanationState.value = AiExplanationState.Loading
            
            // Pause playback when asking AI
            pause()
            
            val result = aiRepository.explainSentence(sentence)
            result.onSuccess { explanation ->
                _aiExplanationState.value = AiExplanationState.Success(explanation)
            }.onFailure { error ->
                _aiExplanationState.value = AiExplanationState.Error(error.message ?: "Failed to get explanation")
            }
        }
    }
    
    fun clearExplanation() {
        _aiExplanationState.value = AiExplanationState.Idle
    }

    private var lesson: Lesson? = null
    private var startTime: Long = 0L
    private var isTrackingProgress = false
    private var allLessons: List<Lesson> = emptyList()
    private var hasLoadedLessons = false

    // For auto-save playback history
    private var lastSavePosition: Long = 0L
    private val SAVE_INTERVAL_MS = 5000L // Save every 5 seconds during playback

    init {
        android.util.Log.d("PlayerViewModel", "Initialized with bookId=$bookId, lessonId=$currentLessonId")
        loadLesson(currentLessonId)
        startPositionTracking()
        observePlaybackState()
        startAutoSavePlaybackHistory()
    }

    private fun loadLesson(lessonIdToLoad: String = currentLessonId) {
        viewModelScope.launch {
            android.util.Log.d("PlayerViewModel", "Loading lesson: bookId=$bookId, lessonId=$lessonIdToLoad")
            _uiState.value = PlayerUiState.Loading

            contentRepository.loadLesson(bookId, lessonIdToLoad)
                .onSuccess { loadedLesson ->
                    android.util.Log.d("PlayerViewModel", "Lesson loaded successfully: ${loadedLesson?.title}")
                    lesson = loadedLesson
                    if (loadedLesson != null) {
                        // Load LRC segments
                        lrcSyncManager.loadSegments(loadedLesson.segments)

                        // Load audio
                        android.util.Log.d("PlayerViewModel", "Loading audio: file=${loadedLesson.audioFile}, lessonId=${loadedLesson.id}")
                        audioManager.loadAudio(loadedLesson.audioFile, loadedLesson.id)

                        // Save vocabulary to database for flashcard learning
                        if (loadedLesson.vocabulary.isNotEmpty()) {
                            android.util.Log.d("PlayerViewModel", "Saving ${loadedLesson.vocabulary.size} vocabulary words")
                            lessonRepository.saveVocabulary(loadedLesson.id, loadedLesson.vocabulary)
                        }

                        // Initialize progress
                        lessonRepository.initializeLessonProgress(loadedLesson.id, bookId)

                        // Update global playback state
                        playbackStateManager.setCurrentLesson(bookId, loadedLesson)

                        // Check if this is the last played lesson and restore position
                        userPreferencesManager.playbackHistory.firstOrNull()?.let { history ->
                            if (history.lessonId == loadedLesson.id && history.position > 0) {
                                android.util.Log.d("PlayerViewModel", "Restoring last position: ${history.position}ms")
                                audioManager.seekTo(history.position)
                                lastSavePosition = history.position
                            }
                        }

                        _uiState.value = PlayerUiState.Success(loadedLesson)
                        android.util.Log.d("PlayerViewModel", "UI State updated to Success")
                    } else {
                        android.util.Log.e("PlayerViewModel", "Lesson is null")
                        _uiState.value = PlayerUiState.Error("Lesson not found")
                    }
                }
                .onFailure { exception ->
                    android.util.Log.e("PlayerViewModel", "Failed to load lesson", exception)
                    _uiState.value = PlayerUiState.Error(
                        exception.message ?: "Failed to load lesson"
                    )
                }
        }
    }

    private fun startPositionTracking() {
        viewModelScope.launch {
            while (true) {
                delay(100) // Update every 100ms for smoother synchronization
                if (audioManager.playbackState.value is PlaybackState.Playing) {
                    val positionSeconds = audioManager.getCurrentPositionSeconds()
                    lrcSyncManager.updatePosition(positionSeconds)

                    // Debug log every 3 seconds (every 30 ticks)
                    if ((positionSeconds * 10).toInt() % 30 == 0) {
                        android.util.Log.d("PlayerViewModel", "Position: ${positionSeconds}s, segment: ${lrcSyncManager.currentSegmentIndex.value}")
                    }
                }
            }
        }
    }

    /**
     * Auto-save playback history every few seconds during playback
     * This ensures that even if the app is killed, the last position is saved
     */
    private fun startAutoSavePlaybackHistory() {
        viewModelScope.launch {
            while (true) {
                delay(SAVE_INTERVAL_MS) // Check every 5 seconds

                val currentLesson = lesson
                val currentPosition = audioManager.currentPosition.value

                // Save if we have a lesson and the position has changed significantly (more than 1 second)
                if (currentLesson != null && currentPosition > 0) {
                    if (kotlin.math.abs(currentPosition - lastSavePosition) > 1000) {
                        userPreferencesManager.savePlaybackHistory(
                            bookId = bookId,
                            lessonId = currentLesson.id,
                            position = currentPosition
                        )
                        lastSavePosition = currentPosition
                        android.util.Log.d("PlayerViewModel", "Auto-saved playback history: ${currentPosition}ms")
                    }
                }
            }
        }
    }

    fun play() {
        audioManager.play()
        playbackStateManager.setPlaying(true)
        startTime = System.currentTimeMillis()
        isTrackingProgress = true
    }

    fun pause() {
        audioManager.pause()
        playbackStateManager.setPlaying(false)
        if (isTrackingProgress) {
            val timeSpent = System.currentTimeMillis() - startTime
            saveProgress(timeSpent)
            isTrackingProgress = false
        }
    }

    fun stop() {
        audioManager.stop()
        // Reset to beginning
        audioManager.seekTo(0)
        lrcSyncManager.jumpToSegment(0)
        if (isTrackingProgress) {
            val timeSpent = System.currentTimeMillis() - startTime
            saveProgress(timeSpent)
            isTrackingProgress = false
        }
    }

    fun seekTo(position: Long) {
        audioManager.seekTo(position)
    }

    fun skipForward() {
        audioManager.skipForward(10000L) // Skip 10 seconds
    }

    fun skipBackward() {
        audioManager.skipBackward(10000L) // Skip 10 seconds
    }

    fun setPlaybackSpeed(speed: Float) {
        audioManager.setPlaybackSpeed(speed)
    }

    fun toggleTranscriptVisibility() {
        viewModelScope.launch {
            // 获取当前值并取反，然后保存到DataStore
            val currentValue = isTranscriptVisible.value
            userPreferencesManager.updateShowTranscriptByDefault(!currentValue)
            android.util.Log.d("PlayerViewModel", "Transcript visibility toggled: ${!currentValue}")
        }
    }

    fun onSegmentClick(index: Int) {
        val startTime = lrcSyncManager.jumpToSegment(index)
        if (startTime != null) {
            audioManager.seekTo((startTime * 1000).toLong())
        }
    }

    private fun saveProgress(timeSpent: Long) {
        val currentLesson = lesson ?: return
        val currentPosition = audioManager.currentPosition.value
        val duration = audioManager.duration.value

        val completionPercentage = if (duration > 0) {
            ((currentPosition.toFloat() / duration.toFloat()) * 100f)
        } else 0f

        val isCompleted = completionPercentage >= 95f

        viewModelScope.launch {
            // Save to database
            lessonRepository.updateLessonProgress(
                lessonId = currentLesson.id,
                bookId = bookId,
                lastPosition = currentPosition,
                completionPercentage = completionPercentage,
                timeSpent = timeSpent,
                isCompleted = isCompleted
            )

            // Save playback history to DataStore
            userPreferencesManager.savePlaybackHistory(
                bookId = bookId,
                lessonId = currentLesson.id,
                position = currentPosition
            )
        }
    }

    /**
     * Observe playback state for auto-play next functionality
     */
    private fun observePlaybackState() {
        viewModelScope.launch {
            audioManager.playbackState.collect { state ->
                if (state is PlaybackState.Ended && autoPlayNext.value) {
                    android.util.Log.d("PlayerViewModel", "Playback ended, auto-playing next lesson")
                    loadNextLesson()
                }
            }
        }
    }

    /**
     * Toggle auto-play next lesson
     */
    fun toggleAutoPlayNext() {
        viewModelScope.launch {
            // 获取当前值并取反，然后保存到DataStore
            val currentValue = autoPlayNext.value
            userPreferencesManager.updateAutoPlayNext(!currentValue)
            android.util.Log.d("PlayerViewModel", "Auto-play next toggled: ${!currentValue}")
        }
    }

    /**
     * Load the next lesson in the book
     */
    private fun loadNextLesson() {
        viewModelScope.launch {
            // Load all lessons if not already loaded
            if (!hasLoadedLessons) {
                contentRepository.loadBookLessons(bookId)
                    .onSuccess { lessons ->
                        allLessons = lessons
                        hasLoadedLessons = true
                        android.util.Log.d("PlayerViewModel", "Loaded ${lessons.size} lessons")
                    }
            }

            // Find current lesson index
            val currentIndex = allLessons.indexOfFirst { it.id == currentLessonId }
            android.util.Log.d("PlayerViewModel", "Current lesson index: $currentIndex, currentLessonId: $currentLessonId")



            
            if (currentIndex != -1 && currentIndex < allLessons.size - 1) {
                // Load next lesson
                val nextLesson = allLessons[currentIndex + 1]
                android.util.Log.d("PlayerViewModel", "Loading next lesson: ${nextLesson.title} (${nextLesson.id})")

                // Update current lesson ID
                currentLessonId = nextLesson.id
                lesson = nextLesson

                // Load LRC segments
                lrcSyncManager.loadSegments(nextLesson.segments)

                // Load audio
                audioManager.loadAudio(nextLesson.audioFile, nextLesson.id)

                // Save vocabulary
                if (nextLesson.vocabulary.isNotEmpty()) {
                    lessonRepository.saveVocabulary(nextLesson.id, nextLesson.vocabulary)
                }

                // Update UI state
                _uiState.value = PlayerUiState.Success(nextLesson)

                // Update global playback state
                playbackStateManager.setCurrentLesson(bookId, nextLesson)

                // Start playing automatically
                audioManager.play()
                playbackStateManager.setPlaying(true)
            } else {
                android.util.Log.d("PlayerViewModel", "No more lessons in this book")
                // Stop auto-play
                viewModelScope.launch {
                    userPreferencesManager.updateAutoPlayNext(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        // Save playback history one last time before being destroyed
        val currentLesson = lesson
        val currentPosition = audioManager.currentPosition.value

        if (currentLesson != null && currentPosition > 0) {
            viewModelScope.launch {
                userPreferencesManager.savePlaybackHistory(
                    bookId = bookId,
                    lessonId = currentLesson.id,
                    position = currentPosition
                )
                android.util.Log.d("PlayerViewModel", "Saved playback history on clear: $currentPosition ms")
            }
        }

        if (isTrackingProgress) {
            pause()
        }
        // Don't clear playback state - allow it to persist for mini player
    }

    sealed class PlayerUiState {
        object Loading : PlayerUiState()
        data class Success(val lesson: Lesson) : PlayerUiState()
        data class Error(val message: String) : PlayerUiState()
    }
    
    sealed class AiExplanationState {
        object Idle : AiExplanationState()
        object Loading : AiExplanationState()
        data class Success(val explanation: String) : AiExplanationState()
        data class Error(val message: String) : AiExplanationState()
    }
}
