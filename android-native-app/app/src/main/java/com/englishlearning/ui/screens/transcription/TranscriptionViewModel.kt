package com.englishlearning.ui.screens.transcription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.model.Segment
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.repository.LessonRepository
import com.englishlearning.ui.screens.player.AudioManager
import com.englishlearning.ui.screens.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for transcription exercise mode
 */
@HiltViewModel
class TranscriptionViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val lessonRepository: LessonRepository,
    private val audioManager: AudioManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>("bookId") ?: "book1"
    private val lessonId: String = savedStateHandle.get<String>("lessonId") ?: "book1_lesson_001"

    private val _uiState = MutableStateFlow<TranscriptionUiState>(TranscriptionUiState.Loading)
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    private val _currentSegmentIndex = MutableStateFlow(0)
    val currentSegmentIndex: StateFlow<Int> = _currentSegmentIndex.asStateFlow()

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _showHint = MutableStateFlow(false)
    val showHint: StateFlow<Boolean> = _showHint.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private var lesson: Lesson? = null
    private var correctAnswers: Int = 0
    private var totalAttempts: Int = 0
    private var segmentEndTime: Long = 0L
    private var isSegmentPlaying = false

    init {
        loadLesson()
        observePlaybackState()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = TranscriptionUiState.Loading

            contentRepository.loadLesson(bookId, lessonId)
                .onSuccess { loadedLesson ->
                    lesson = loadedLesson
                    if (loadedLesson != null && loadedLesson.segments.isNotEmpty()) {
                        _uiState.value = TranscriptionUiState.Ready(loadedLesson)
                    } else {
                        _uiState.value = TranscriptionUiState.Error("Lesson has no segments")
                    }
                }
                .onFailure { exception ->
                    _uiState.value = TranscriptionUiState.Error(
                        exception.message ?: "Failed to load lesson"
                    )
                }
        }
    }

    fun playSegment(index: Int) {
        val currentLesson = lesson ?: return
        if (index !in currentLesson.segments.indices) return
        
        val segment = currentLesson.segments[index]
        _currentSegmentIndex.value = index
        _isPlaying.value = true
        isSegmentPlaying = true

        viewModelScope.launch {
            try {
                // Load audio if not already loaded
                if (audioManager.currentLessonId != currentLesson.id) {
                    audioManager.loadAudio(currentLesson.audioFile, currentLesson.id)
                    // Wait for audio to be ready
                    delay(500)
                }

                // Calculate segment times in milliseconds
                val startTimeMs = (segment.startTime * 1000).toLong()
                val endTimeMs = if (segment.endTime > 0) {
                    (segment.endTime * 1000).toLong()
                } else {
                    // Estimate end time: use next segment's start time or add 5 seconds
                    val nextSegment = currentLesson.segments.getOrNull(index + 1)
                    if (nextSegment != null) {
                        (nextSegment.startTime * 1000).toLong()
                    } else {
                        startTimeMs + 5000 // Default 5 seconds
                    }
                }
                segmentEndTime = endTimeMs

                // Seek to start time and play
                audioManager.seekTo(startTimeMs)
                audioManager.play()

                // Monitor playback position to stop at end time
                while (isSegmentPlaying && audioManager.playbackState.value is PlaybackState.Playing) {
                    val currentPosition = audioManager.currentPosition.value
                    if (currentPosition >= endTimeMs) {
                        audioManager.pause()
                        _isPlaying.value = false
                        isSegmentPlaying = false
                        break
                    }
                    delay(100) // Check every 100ms
                }
            } catch (e: Exception) {
                android.util.Log.e("TranscriptionViewModel", "Error playing segment", e)
                _isPlaying.value = false
                isSegmentPlaying = false
            }
        }
    }

    /**
     * Observe playback state to update UI when playback stops
     */
    private fun observePlaybackState() {
        viewModelScope.launch {
            audioManager.playbackState.collect { state ->
                if (state !is PlaybackState.Playing && isSegmentPlaying) {
                    _isPlaying.value = false
                    isSegmentPlaying = false
                }
            }
        }
    }

    fun onUserInputChange(input: String) {
        _userInput.value = input
    }

    fun submitAnswer() {
        val currentLesson = lesson ?: return
        val currentSegment = currentLesson.segments.getOrNull(_currentSegmentIndex.value)
        val userText = _userInput.value.trim()

        if (currentSegment == null || userText.isEmpty()) return

        totalAttempts++

        val isCorrect = compareText(userText, currentSegment.text)
        if (isCorrect) {
            correctAnswers++
            _score.value = ((correctAnswers.toFloat() / totalAttempts.toFloat()) * 100).toInt()
            _uiState.value = TranscriptionUiState.Correct(
                lesson = currentLesson,
                segmentIndex = _currentSegmentIndex.value,
                userText = userText,
                correctText = currentSegment.text
            )
        } else {
            _uiState.value = TranscriptionUiState.Incorrect(
                lesson = currentLesson,
                segmentIndex = _currentSegmentIndex.value,
                userText = userText,
                correctText = currentSegment.text,
                differences = highlightDifferences(userText, currentSegment.text)
            )
        }
    }

    fun showHint() {
        _showHint.value = true
    }

    fun revealAnswer() {
        val currentLesson = lesson ?: return
        val currentSegment = currentLesson.segments.getOrNull(_currentSegmentIndex.value)
        if (currentSegment != null) {
            _uiState.value = TranscriptionUiState.Revealed(
                lesson = currentLesson,
                segmentIndex = _currentSegmentIndex.value,
                correctText = currentSegment.text
            )
        }
    }

    fun nextSegment() {
        val currentLesson = lesson ?: return
        if (_currentSegmentIndex.value < currentLesson.segments.size - 1) {
            _currentSegmentIndex.value++
            _userInput.value = ""
            _showHint.value = false
            _isPlaying.value = false
            _uiState.value = TranscriptionUiState.Ready(currentLesson)
        } else {
            // Exercise completed
            _uiState.value = TranscriptionUiState.Completed(
                lesson = currentLesson,
                score = _score.value,
                correctAnswers = correctAnswers,
                totalSegments = totalAttempts
            )
        }
    }

    fun retrySegment() {
        _userInput.value = ""
        _showHint.value = false
        _isPlaying.value = false
        isSegmentPlaying = false
        audioManager.pause()
        val currentLesson = lesson ?: return
        _uiState.value = TranscriptionUiState.Ready(currentLesson)
    }

    fun resetExercise() {
        _currentSegmentIndex.value = 0
        _userInput.value = ""
        _showHint.value = false
        _isPlaying.value = false
        isSegmentPlaying = false
        audioManager.pause()
        _score.value = 0
        correctAnswers = 0
        totalAttempts = 0
        val currentLesson = lesson
        if (currentLesson != null) {
            _uiState.value = TranscriptionUiState.Ready(currentLesson)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop any ongoing playback
        if (isSegmentPlaying) {
            audioManager.pause()
        }
    }

    /**
     * Compare user input with correct text
     * Returns true if they match (case-insensitive, ignoring punctuation)
     */
    private fun compareText(userText: String, correctText: String): Boolean {
        val normalizedUser = normalizeText(userText)
        val normalizedCorrect = normalizeText(correctText)
        return normalizedUser.equals(normalizedCorrect, ignoreCase = true)
    }

    /**
     * Normalize text for comparison (remove punctuation, extra spaces)
     */
    private fun normalizeText(text: String): String {
        return text
            .replace(Regex("[.,!?;:'\"\\-]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Highlight differences between user text and correct text
     * Returns a list of Diff objects showing where they differ
     */
    private fun highlightDifferences(userText: String, correctText: String): List<Diff> {
        val diffs = mutableListOf<Diff>()
        val userWords = userText.split(Regex("\\s+"))
        val correctWords = correctText.split(Regex("\\s+"))

        val maxLength = maxOf(userWords.size, correctWords.size)

        for (i in 0 until maxLength) {
            val userWord = userWords.getOrNull(i) ?: ""
            val correctWord = correctWords.getOrNull(i) ?: ""

            if (userWord != correctWord) {
                diffs.add(
                    Diff(
                        index = i,
                        userWord = if (userWord.isEmpty()) null else userWord,
                        correctWord = if (correctWord.isEmpty()) null else correctWord,
                        type = when {
                            userWord.isEmpty() -> DiffType.MISSING
                            correctWord.isEmpty() -> DiffType.EXTRA
                            else -> DiffType.WRONG
                        }
                    )
                )
            }
        }

        return diffs
    }

    /**
     * Get current segment
     */
    fun getCurrentSegment(): Segment? {
        val currentLesson = lesson ?: return null
        return currentLesson.segments.getOrNull(_currentSegmentIndex.value)
    }

    /**
     * Get hint for current segment (first letter of each word)
     */
    fun getHint(): String {
        val segment = getCurrentSegment() ?: return ""
        return segment.text
            .split(Regex("\\s+"))
            .joinToString(" ") { word ->
                if (word.isNotEmpty()) word[0] + ". " else ""
            }
    }

    sealed class TranscriptionUiState {
        object Loading : TranscriptionUiState()
        data class Ready(val lesson: Lesson) : TranscriptionUiState()
        data class Correct(
            val lesson: Lesson,
            val segmentIndex: Int,
            val userText: String,
            val correctText: String
        ) : TranscriptionUiState()

        data class Incorrect(
            val lesson: Lesson,
            val segmentIndex: Int,
            val userText: String,
            val correctText: String,
            val differences: List<Diff>
        ) : TranscriptionUiState()

        data class Revealed(
            val lesson: Lesson,
            val segmentIndex: Int,
            val correctText: String
        ) : TranscriptionUiState()

        data class Completed(
            val lesson: Lesson,
            val score: Int,
            val correctAnswers: Int,
            val totalSegments: Int
        ) : TranscriptionUiState()

        data class Error(val message: String) : TranscriptionUiState()
    }

    data class Diff(
        val index: Int,
        val userWord: String?,
        val correctWord: String?,
        val type: DiffType
    )

    enum class DiffType {
        WRONG,      // User typed wrong word
        MISSING,    // User missed this word
        EXTRA       // User typed extra word
    }
}
