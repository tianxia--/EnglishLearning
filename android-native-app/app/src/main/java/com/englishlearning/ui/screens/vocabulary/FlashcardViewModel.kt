package com.englishlearning.ui.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Vocabulary
import com.englishlearning.data.local.dao.VocabularyProgressDao
import com.englishlearning.data.local.database.VocabularyProgressEntity
import com.englishlearning.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for vocabulary flashcards with spaced repetition
 */
@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val vocabularyProgressDao: VocabularyProgressDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlashcardUiState>(FlashcardUiState.Loading)
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _studySession = MutableStateFlow<StudySession?>(null)
    val studySession: StateFlow<StudySession?> = _studySession.asStateFlow()

    private var allWords: List<VocabularyWithProgress> = emptyList()
    private var studyQueue: MutableList<VocabularyWithProgress> = mutableListOf()

    init {
        loadVocabulary()
    }

    private fun loadVocabulary() {
        viewModelScope.launch {
            _uiState.value = FlashcardUiState.Loading

            try {
                // Load all vocabulary progress from database
                val allVocab = lessonRepository.getAllVocabulary()

                // Combine with progress data
                allWords = allVocab.mapNotNull { entity ->
                    val vocab = Vocabulary(
                        word = entity.word,
                        definition = "",
                        context = "",
                        timestamp = 0.0
                    )
                    VocabularyWithProgress(
                        vocabulary = vocab,
                        progress = entity
                    )
                }.sortedBy { it.progress.reviewCount }

                if (allWords.isNotEmpty()) {
                    startStudySession()
                } else {
                    _uiState.value = FlashcardUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = FlashcardUiState.Error(
                    e.message ?: "Failed to load vocabulary"
                )
            }
        }
    }

    private fun startStudySession() {
        // Spaced repetition: prioritize words that need review
        studyQueue = allWords
            .filter { !it.progress.isMastered }
            .sortedBy { it.progress.lastReviewDate ?: 0 }
            .take(20) // Study up to 20 words at a time
            .toMutableList()

        if (studyQueue.isNotEmpty()) {
            _studySession.value = StudySession(
                totalCards = studyQueue.size,
                correctCount = 0,
                incorrectCount = 0
            )
            showNextCard()
        } else {
            _uiState.value = FlashcardUiState.AllMastered
        }
    }

    private fun showNextCard() {
        _isFlipped.value = false

        if (studyQueue.isEmpty()) {
            // Session complete
            val session = _studySession.value ?: return
            _uiState.value = FlashcardUiState.SessionComplete(
                totalCards = session.totalCards,
                correctCount = session.correctCount,
                incorrectCount = session.incorrectCount
            )
            return
        }

        val currentCard = studyQueue.first()
        _uiState.value = FlashcardUiState.Study(
            vocabulary = currentCard.vocabulary,
            progress = currentCard.progress,
            currentIndex = _studySession.value?.totalCards?.minus(studyQueue.size) ?: 0,
            totalCards = _studySession.value?.totalCards ?: 0
        )
    }

    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
    }

    fun markAsKnown() {
        val currentCard = studyQueue.removeFirst()
        val session = _studySession.value ?: return

        viewModelScope.launch {
            // Update progress
            lessonRepository.updateVocabularyReview(
                word = currentCard.progress.word,
                isCorrect = true
            )

            // Update session stats
            _studySession.value = session.copy(
                correctCount = session.correctCount + 1,
                totalCards = session.totalCards
            )

            showNextCard()
        }
    }

    fun markAsLearning() {
        val currentCard = studyQueue.removeFirst()
        val session = _studySession.value ?: return

        viewModelScope.launch {
            // Update progress
            lessonRepository.updateVocabularyReview(
                word = currentCard.progress.word,
                isCorrect = false
            )

            // Move to end of queue for more practice
            studyQueue.add(currentCard)

            // Update session stats
            _studySession.value = session.copy(
                incorrectCount = session.incorrectCount + 1,
                totalCards = session.totalCards + 1 // +1 because we re-added the card
            )

            showNextCard()
        }
    }

    fun resetSession() {
        startStudySession()
    }

    fun loadNewVocabulary() {
        loadVocabulary()
    }

    data class StudySession(
        val totalCards: Int,
        val correctCount: Int,
        val incorrectCount: Int
    ) {
        val progress: Int get() = correctCount + incorrectCount
        val accuracy: Float get() =
            if (progress > 0) correctCount.toFloat() / progress.toFloat() else 0f
    }

    data class VocabularyWithProgress(
        val vocabulary: Vocabulary,
        val progress: VocabularyProgressEntity
    )

    sealed class FlashcardUiState {
        object Loading : FlashcardUiState()
        object Empty : FlashcardUiState()
        object AllMastered : FlashcardUiState()
        data class Study(
            val vocabulary: Vocabulary,
            val progress: VocabularyProgressEntity,
            val currentIndex: Int,
            val totalCards: Int
        ) : FlashcardUiState()

        data class SessionComplete(
            val totalCards: Int,
            val correctCount: Int,
            val incorrectCount: Int
        ) : FlashcardUiState()

        data class Error(val message: String) : FlashcardUiState()
    }
}
