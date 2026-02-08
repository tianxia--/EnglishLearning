package com.englishlearning.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user progress
 */
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val id: Int = 1, // Single row for user progress
    val currentBookId: String,
    val currentLessonId: String?,
    val totalStudyTime: Long,
    val streakDays: Int,
    val lastStudyDate: Long?,
    val masteredWords: Int,
    val completedQuizzes: Int,
    val quizScoreAverage: Float
)
