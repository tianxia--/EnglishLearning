package com.englishlearning.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing lesson progress
 */
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey
    val lessonId: String,
    val bookId: String,
    val progress: String, // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val lastPosition: Long, // Last playback position in milliseconds
    val completionPercentage: Float,
    val totalTimeSpent: Long, // Total time spent on this lesson in milliseconds
    val lastStudyDate: Long? // Timestamp of last study session
)
