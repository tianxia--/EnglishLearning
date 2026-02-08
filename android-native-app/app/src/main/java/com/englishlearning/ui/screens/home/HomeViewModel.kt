package com.englishlearning.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Book
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.preferences.PlaybackHistory
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.preferences.UserPreferencesManager
import com.englishlearning.domain.DailyTask
import com.englishlearning.domain.TaskGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val taskGenerator: TaskGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _playbackHistory = MutableStateFlow<PlaybackHistoryWithLesson?>(null)
    val playbackHistory: StateFlow<PlaybackHistoryWithLesson?> = _playbackHistory.asStateFlow()

    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks.asStateFlow()

    init {
        loadBooks()
        loadPlaybackHistory()
        loadDailyTasks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            android.util.Log.d("HomeViewModel", "Loading books...")

            contentRepository.loadAllBooks()
                .onSuccess { books ->
                    android.util.Log.d("HomeViewModel", "Successfully loaded ${books.size} books")
                    books.forEach { book ->
                        android.util.Log.d("HomeViewModel", "Book: ${book.id} - ${book.title} (${book.lessonCount} lessons)")
                    }
                    _uiState.value = HomeUiState.Success(books)
                }
                .onFailure { exception ->
                    android.util.Log.e("HomeViewModel", "Failed to load books", exception)
                    _uiState.value = HomeUiState.Error(
                        message = exception.message ?: "Failed to load books"
                    )
                }
        }
    }

    /**
     * Load playback history and associated lesson details
     */
    private fun loadDailyTasks() {
        viewModelScope.launch {
            try {
                // Wait for books to load? Or just load independently.
                // TaskGenerator loads books internally if needed or we can pass them.
                // Current impl of TaskGenerator loads books effectively.
                val tasks = taskGenerator.generateDailyTasks()
                _dailyTasks.value = tasks
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Failed to generate tasks", e)
            }
        }
    }
    
    /**
     * Load playback history and associated lesson details
     */
    private fun loadPlaybackHistory() {
        viewModelScope.launch {
            userPreferencesManager.playbackHistory.collect { history ->
                if (history != null) {
                    android.util.Log.d("HomeViewModel", "Loading playback history: ${history.bookId}/${history.lessonId}")

                    // Load lesson details
                    contentRepository.loadLesson(history.bookId, history.lessonId)
                        .onSuccess { lesson ->
                            if (lesson != null) {
                                _playbackHistory.value = PlaybackHistoryWithLesson(
                                    history = history,
                                    lesson = lesson
                                )
                                android.util.Log.d("HomeViewModel", "Playback history loaded: ${lesson.title}")
                            } else {
                                _playbackHistory.value = null
                                android.util.Log.d("HomeViewModel", "Lesson not found in playback history")
                            }
                        }
                        .onFailure { exception ->
                            android.util.Log.e("HomeViewModel", "Failed to load lesson from playback history", exception)
                            _playbackHistory.value = null
                        }
                } else {
                    _playbackHistory.value = null
                    android.util.Log.d("HomeViewModel", "No playback history found")
                }
            }
        }
    }

    // Navigation is handled by the UI layer
    // This method is no longer needed but kept for reference

    sealed class HomeUiState {
        object Loading : HomeUiState()
        data class Success(val books: List<Book>) : HomeUiState()
        data class Error(val message: String) : HomeUiState()
    }
}

/**
 * Data class holding playback history with associated lesson details
 */
data class PlaybackHistoryWithLesson(
    val history: PlaybackHistory,
    val lesson: Lesson
)

