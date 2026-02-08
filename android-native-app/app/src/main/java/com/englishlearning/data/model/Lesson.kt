package com.englishlearning.data.model

import kotlinx.serialization.Serializable

/**
 * Data model for a New Concept English lesson
 */
@Serializable
data class Lesson(
    val id: String,
    val bookId: String,
    val lessonNumber: Int,
    val title: String,
    val audioFile: String,
    val lrcFile: String,
    val duration: Double,
    val segments: List<Segment> = emptyList(),
    val vocabulary: List<Vocabulary> = emptyList(),
    val quizzes: List<Quiz> = emptyList(),
    var progress: LessonProgress = LessonProgress.NOT_STARTED,
    var lastPosition: Long = 0L, // Last playback position in milliseconds
    var completionPercentage: Float = 0f
)

/**
 * Synchronized text segment from LRC file
 */
@Serializable
data class Segment(
    val startTime: Double, // Start time in seconds
    val endTime: Double = 0.0, // End time in seconds (calculated if not provided)
    val text: String
)

/**
 * Vocabulary word from lesson
 */
@Serializable
data class Vocabulary(
    val word: String,
    val definition: String = "",
    val context: String = "",
    val timestamp: Double = 0.0,
    var isMastered: Boolean = false,
    var reviewCount: Int = 0,
    var lastReviewDate: Long? = null
)

/**
 * Quiz question
 */
@Serializable
data class Quiz(
    val type: QuizType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: Int,
    val explanation: String = "",
    var userAnswer: Int? = null,
    var isCorrect: Boolean? = null
)

@Serializable
enum class QuizType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    FILL_BLANK
}

/**
 * Lesson progress status
 */
enum class LessonProgress {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
