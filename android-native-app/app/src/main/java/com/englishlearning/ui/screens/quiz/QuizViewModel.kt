package com.englishlearning.ui.screens.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.model.Quiz
import com.englishlearning.data.model.QuizType
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for comprehension quizzes
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>("bookId") ?: "book1"
    private val lessonId: String = savedStateHandle.get<String>("lessonId") ?: "book1_lesson_001"

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private var lesson: Lesson? = null
    private var quizzes: List<Quiz> = emptyList()
    private var userAnswers: MutableList<Int?> = mutableListOf()
    private var correctCount: Int = 0

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading

            contentRepository.loadLesson(bookId, lessonId)
                .onSuccess { loadedLesson ->
                    lesson = loadedLesson
                    if (loadedLesson != null) {
                        // For now, create sample quizzes if lesson doesn't have any
                        quizzes = if (loadedLesson.quizzes.isNotEmpty()) {
                            loadedLesson.quizzes
                        } else {
                            createSampleQuizzes(loadedLesson)
                        }

                        if (quizzes.isNotEmpty()) {
                            _uiState.value = QuizUiState.Question(
                                lesson = loadedLesson,
                                quiz = quizzes[0],
                                questionIndex = 0,
                                totalQuestions = quizzes.size
                            )
                        } else {
                            _uiState.value = QuizUiState.Error("No quizzes available for this lesson")
                        }
                    } else {
                        _uiState.value = QuizUiState.Error("Lesson not found")
                    }
                }
                .onFailure { exception ->
                    _uiState.value = QuizUiState.Error(
                        exception.message ?: "Failed to load lesson"
                    )
                }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
    }

    fun submitAnswer() {
        val answerIndex = _selectedAnswer.value
        if (answerIndex == null) return

        val currentQuiz = quizzes.getOrNull(_currentQuestionIndex.value) ?: return
        val isCorrect = answerIndex == currentQuiz.correctAnswer

        if (isCorrect) {
            correctCount++
            _score.value = ((correctCount.toFloat() / quizzes.size.toFloat()) * 100).toInt()
        }

        userAnswers.add(answerIndex)

        _uiState.value = QuizUiState.Feedback(
            lesson = lesson!!,
            quiz = currentQuiz,
            questionIndex = _currentQuestionIndex.value,
            totalQuestions = quizzes.size,
            userAnswer = answerIndex,
            isCorrect = isCorrect,
            explanation = currentQuiz.explanation
        )
    }

    fun nextQuestion() {
        _selectedAnswer.value = null

        if (_currentQuestionIndex.value < quizzes.size - 1) {
            _currentQuestionIndex.value++
            _uiState.value = QuizUiState.Question(
                lesson = lesson!!,
                quiz = quizzes[_currentQuestionIndex.value],
                questionIndex = _currentQuestionIndex.value,
                totalQuestions = quizzes.size
            )
        } else {
            // Quiz completed
            _uiState.value = QuizUiState.Completed(
                lesson = lesson!!,
                score = _score.value,
                correctAnswers = correctCount,
                totalQuestions = quizzes.size,
                userAnswers = userAnswers.toList()
            )
        }
    }

    fun retryQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswer.value = null
        _score.value = 0
        correctCount = 0
        userAnswers.clear()

        if (quizzes.isNotEmpty()) {
            _uiState.value = QuizUiState.Question(
                lesson = lesson!!,
                quiz = quizzes[0],
                questionIndex = 0,
                totalQuestions = quizzes.size
            )
        }
    }

    /**
     * Create sample quizzes for a lesson (if none exist)
     */
    private fun createSampleQuizzes(lesson: Lesson): List<Quiz> {
        // In a real implementation, these would be in the lesson data
        // For now, we'll generate sample questions based on the content
        val sampleQuizzes = mutableListOf<Quiz>()

        // Add a sample comprehension question
        sampleQuizzes.add(
            Quiz(
                type = QuizType.MULTIPLE_CHOICE,
                question = "What is the main topic of this lesson?",
                options = listOf(
                    "Greetings and introductions",
                    "Business meeting",
                    "Weather discussion",
                    "Food ordering"
                ),
                correctAnswer = 0,
                explanation = "This lesson focuses on basic greetings and introductions."
            )
        )

        // Add a true/false question
        sampleQuizzes.add(
            Quiz(
                type = QuizType.TRUE_FALSE,
                question = "The speaker uses formal language throughout the lesson.",
                options = listOf("True", "False"),
                correctAnswer = 1,
                explanation = "The speaker uses casual, everyday language."
            )
        )

        // Add a fill-in-the-blank question
        sampleQuizzes.add(
            Quiz(
                type = QuizType.FILL_BLANK,
                question = "Complete: '_____ me!'",
                options = listOf(
                    "Excuse",
                    "Hello",
                    "Please",
                    "Thank"
                ),
                correctAnswer = 0,
                explanation = "The phrase is 'Excuse me!'"
            )
        )

        return sampleQuizzes
    }

    /**
     * Get current quiz
     */
    fun getCurrentQuiz(): Quiz? {
        return quizzes.getOrNull(_currentQuestionIndex.value)
    }

    sealed class QuizUiState {
        object Loading : QuizUiState()
        data class Question(
            val lesson: Lesson,
            val quiz: Quiz,
            val questionIndex: Int,
            val totalQuestions: Int
        ) : QuizUiState()

        data class Feedback(
            val lesson: Lesson,
            val quiz: Quiz,
            val questionIndex: Int,
            val totalQuestions: Int,
            val userAnswer: Int,
            val isCorrect: Boolean,
            val explanation: String
        ) : QuizUiState()

        data class Completed(
            val lesson: Lesson,
            val score: Int,
            val correctAnswers: Int,
            val totalQuestions: Int,
            val userAnswers: List<Int?>
        ) : QuizUiState()

        data class Error(val message: String) : QuizUiState()
    }
}
