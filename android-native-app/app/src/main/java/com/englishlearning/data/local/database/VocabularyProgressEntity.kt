package com.englishlearning.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing vocabulary progress
 */
@Entity(tableName = "vocabulary_progress")
data class VocabularyProgressEntity(
    @PrimaryKey
    val word: String,
    val lessonId: String,
    val isMastered: Boolean,
    val reviewCount: Int,
    val lastReviewDate: Long?,
    val correctCount: Int,
    val incorrectCount: Int
)
