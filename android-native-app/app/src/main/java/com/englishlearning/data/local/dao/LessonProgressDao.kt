package com.englishlearning.data.local.dao

import androidx.room.*
import com.englishlearning.data.local.database.LessonProgressEntity

/**
 * DAO for lesson progress operations
 */
@Dao
interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun getProgress(lessonId: String): LessonProgressEntity?

    @Query("SELECT * FROM lesson_progress WHERE bookId = :bookId ORDER BY lessonId")
    suspend fun getBookProgress(bookId: String): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress")
    suspend fun getAllProgress(): List<LessonProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LessonProgressEntity)

    @Update
    suspend fun updateProgress(progress: LessonProgressEntity)

    @Delete
    suspend fun deleteProgress(progress: LessonProgressEntity)

    @Query("DELETE FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun deleteByLessonId(lessonId: String)

    @Query("UPDATE lesson_progress SET progress = :progress, lastPosition = :lastPosition, completionPercentage = :completionPercentage, totalTimeSpent = totalTimeSpent + :timeSpent, lastStudyDate = :lastStudyDate WHERE lessonId = :lessonId")
    suspend fun updateStudyProgress(
        lessonId: String,
        progress: String,
        lastPosition: Long,
        completionPercentage: Float,
        timeSpent: Long,
        lastStudyDate: Long
    )
}
