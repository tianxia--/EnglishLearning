package com.englishlearning.data.model

import kotlinx.serialization.Serializable

/**
 * Data model for a New Concept English book
 */
@Serializable
data class Book(
    val id: String,
    val title: String,
    val level: String, // CEFR level (A1-C1)
    val description: String,
    val audioPath: String,
    val pdfPath: String,
    val lessonCount: Int,
    val lessons: List<Lesson> = emptyList(),
    var completedLessons: Int = 0,
    var totalTimeSpent: Long = 0L // Total time spent in milliseconds
) {
    val progressPercentage: Float
        get() = if (lessonCount > 0) {
            (completedLessons.toFloat() / lessonCount.toFloat()) * 100f
        } else 0f
}
