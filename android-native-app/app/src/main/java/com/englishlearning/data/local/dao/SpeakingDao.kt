package com.englishlearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.englishlearning.data.local.database.SpeakingRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeakingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakingRecord(record: SpeakingRecordEntity)

    @Query("SELECT * FROM speaking_records WHERE lessonId = :lessonId ORDER BY segmentIndex ASC")
    fun getRecordsForLesson(lessonId: String): Flow<List<SpeakingRecordEntity>>

    @Query("SELECT * FROM speaking_records WHERE lessonId = :lessonId AND segmentIndex = :segmentIndex ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRecordForSegment(lessonId: String, segmentIndex: Int): SpeakingRecordEntity?

    @Query("SELECT AVG(accuracyScore) FROM speaking_records WHERE lessonId = :lessonId")
    suspend fun getAverageAccuracyForLesson(lessonId: String): Float?
    
    @Query("SELECT * FROM speaking_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<SpeakingRecordEntity>>
}
