package com.englishlearning.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.ui.screens.home.LessonsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for loading lessons
 */
@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonsUiState>(LessonsUiState.Loading)
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    fun loadLessons(bookId: String) {
        viewModelScope.launch {
            android.util.Log.d("LessonsViewModel", "Loading lessons for book: $bookId")
            _uiState.value = LessonsUiState.Loading

            contentRepository.loadBookLessons(bookId)
                .onSuccess { lessons ->
                    android.util.Log.d("LessonsViewModel", "Successfully loaded ${lessons.size} lessons")
                    _uiState.value = LessonsUiState.Success(lessons)
                }
                .onFailure { exception ->
                    android.util.Log.e("LessonsViewModel", "Failed to load lessons", exception)
                    _uiState.value = LessonsUiState.Error(
                        exception.message ?: "Failed to load lessons"
                    )
                }
        }
    }
}
