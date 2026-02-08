package com.englishlearning.data.repository

import com.englishlearning.data.local.dao.SpeakingDao
import com.englishlearning.data.local.database.SpeakingRecordEntity
import com.englishlearning.data.local.database.toSpeakingRecord
import com.englishlearning.data.model.SpeakingRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing speaking records and scores
 */
@Singleton
class SpeakingRepository @Inject constructor(
    private val speakingDao: SpeakingDao
) {

    /**
     * Save a new speaking record
     */
    suspend fun saveSpeakingRecord(record: SpeakingRecord) = withContext(Dispatchers.IO) {
        speakingDao.insertSpeakingRecord(
            SpeakingRecordEntity(
                id = record.id,
                lessonId = record.lessonId,
                bookId = record.bookId,
                segmentIndex = record.segmentIndex,
                text = record.text,
                audioPath = record.audioPath,
                accuracyScore = record.accuracyScore,
                fluencyScore = record.fluencyScore,
                timestamp = record.timestamp
            )
        )
    }

    /**
     * Get all records for a specific lesson
     */
    fun getRecordsForLesson(lessonId: String): Flow<List<SpeakingRecord>> {
        return speakingDao.getRecordsForLesson(lessonId).map { entities ->
            entities.map { it.toSpeakingRecord() }
        }
    }

    /**
     * Get the latest record for a specific segment
     */
    suspend fun getLatestRecordForSegment(lessonId: String, segmentIndex: Int): SpeakingRecord? = withContext(Dispatchers.IO) {
        speakingDao.getLatestRecordForSegment(lessonId, segmentIndex)?.toSpeakingRecord()
    }

    /**
     * Get average accuracy score for a lesson
     */
    suspend fun getLessonAverageAccuracy(lessonId: String): Float = withContext(Dispatchers.IO) {
        speakingDao.getAverageAccuracyForLesson(lessonId) ?: 0f
    }
}
