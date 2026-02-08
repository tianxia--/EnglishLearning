package com.englishlearning.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a user's speaking attempt for a specific segment
 */
@Serializable
data class SpeakingRecord(
    val id: String,
    val lessonId: String,
    val bookId: String,
    val segmentIndex: Int,
    val text: String,
    val audioPath: String, // Local path to the recorded audio file
    val accuracyScore: Float, // 0-100 score based on pronunciation/accuracy
    val fluencyScore: Float, // 0-100 score based on fluency/speed
    val timestamp: Long
)
