package com.englishlearning.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.englishlearning.data.model.SpeakingRecord

@Entity(tableName = "speaking_records")
data class SpeakingRecordEntity(
    @PrimaryKey
    val id: String,
    val lessonId: String,
    val bookId: String,
    val segmentIndex: Int,
    val text: String,
    val audioPath: String,
    val accuracyScore: Float,
    val fluencyScore: Float,
    val timestamp: Long
)

// Extension function to map Entity to Domain Model
fun SpeakingRecordEntity.toSpeakingRecord(): SpeakingRecord {
    return SpeakingRecord(
        id = id,
        lessonId = lessonId,
        bookId = bookId,
        segmentIndex = segmentIndex,
        text = text,
        audioPath = audioPath,
        accuracyScore = accuracyScore,
        fluencyScore = fluencyScore,
        timestamp = timestamp
    )
}

// Extension function to map Domain Model to Entity
fun SpeakingRecord.toEntity(): SpeakingRecordEntity {
    return SpeakingRecordEntity(
        id = id,
        lessonId = lessonId,
        bookId = bookId,
        segmentIndex = segmentIndex,
        text = text,
        audioPath = audioPath,
        accuracyScore = accuracyScore,
        fluencyScore = fluencyScore,
        timestamp = timestamp
    )
}
