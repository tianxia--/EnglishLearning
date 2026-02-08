package com.englishlearning.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for progress screen
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _stats = MutableStateFlow(
        ProgressStats(
            completedLessons = 0,
            totalStudyMinutes = 0,
            todayStudyMinutes = 0,
            weeklyStudyMinutes = 0,
            currentStreak = 0,
            longestStreak = 0,
            totalVocabularyWords = 0,
            masteredWords = 0,
            learningWords = 0,
            bookProgress = emptyList()
        )
    )
    val stats: StateFlow<ProgressStats> = _stats.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            _uiState.value = ProgressUiState.Loading

            try {
                // Load lesson progress
                val allProgress = lessonRepository.getAllLessonProgress()
                val completedCount = allProgress.count { it.progress == "COMPLETED" }
                val totalTimeSpent = allProgress.sumOf { it.totalTimeSpent } / 1000 / 60 // Convert to minutes

                // Calculate today's study time (simplified - in real app, filter by date)
                val todayMinutes = (totalTimeSpent / 7).toInt() // Estimate
                val weeklyMinutes = totalTimeSpent.toInt()

                // Load vocabulary progress, and auto-seed if empty
                var allVocabulary = lessonRepository.getAllVocabulary()
                if (allVocabulary.isEmpty()) {
                    // Attempt to preload vocabulary from the first book
                    preloadVocabularyFromBook("book1")
                    allVocabulary = lessonRepository.getAllVocabulary()
                }

                val totalWords = allVocabulary.size
                val masteredWords = allVocabulary.count { it.isMastered }
                val learningWords = totalWords - masteredWords

                // Calculate book progress (simplified)
                val bookProgress = listOf(
                    BookProgress("新概念英语第一册", 72, 10, 14f),
                    BookProgress("新概念英语第二册", 96, 5, 5f),
                    BookProgress("新概念英语第三册", 60, 0, 0f),
                    BookProgress("新概念英语第四册", 48, 0, 0f)
                )

                _stats.value = ProgressStats(
                    completedLessons = completedCount,
                    totalStudyMinutes = totalTimeSpent.toInt(),
                    todayStudyMinutes = todayMinutes,
                    weeklyStudyMinutes = weeklyMinutes,
                    currentStreak = 3, // TODO: Calculate from actual data
                    longestStreak = 7, // TODO: Calculate from actual data
                    totalVocabularyWords = totalWords,
                    masteredWords = masteredWords,
                    learningWords = learningWords,
                    bookProgress = bookProgress
                )

                _uiState.value = ProgressUiState.Success
            } catch (e: Exception) {
                android.util.Log.e("ProgressViewModel", "Error loading progress", e)
                _uiState.value = ProgressUiState.Error(
                    e.message ?: "Failed to load progress"
                )
            }
        }
    }

    fun refresh() {
        loadProgress()
    }

    /**
     * Preload vocabulary into the database from a specific book
     */
    private suspend fun preloadVocabularyFromBook(bookId: String) {
        contentRepository.loadBookLessons(bookId)
            .onSuccess { lessons ->
                lessons.forEach { lesson ->
                    if (lesson.vocabulary.isNotEmpty()) {
                        lessonRepository.saveVocabulary(lesson.id, lesson.vocabulary)
                    }
                }
            }
    }

    sealed class ProgressUiState {
        object Loading : ProgressUiState()
        object Success : ProgressUiState()
        data class Error(val message: String) : ProgressUiState()
    }
}

/**
 * Data class for progress statistics
 */
data class ProgressStats(
    val completedLessons: Int,
    val totalStudyMinutes: Int,
    val todayStudyMinutes: Int,
    val weeklyStudyMinutes: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalVocabularyWords: Int,
    val masteredWords: Int,
    val learningWords: Int,
    val bookProgress: List<BookProgress>
)

/**
 * Data class for book progress
 */
data class BookProgress(
    val bookTitle: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val completionPercentage: Float
)
