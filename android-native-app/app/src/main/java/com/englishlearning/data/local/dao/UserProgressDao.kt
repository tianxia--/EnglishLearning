package com.englishlearning.data.local.dao

import androidx.room.*
import com.englishlearning.data.local.database.UserProgressEntity

/**
 * DAO for user progress operations
 */
@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgress(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)

    @Query("UPDATE user_progress SET currentBookId = :bookId, currentLessonId = :lessonId WHERE id = 1")
    suspend fun updateCurrentLesson(bookId: String, lessonId: String?)

    @Query("UPDATE user_progress SET totalStudyTime = totalStudyTime + :timeSpent, lastStudyDate = :lastStudyDate WHERE id = 1")
    suspend fun addStudyTime(timeSpent: Long, lastStudyDate: Long)

    @Query("UPDATE user_progress SET streakDays = :streakDays WHERE id = 1")
    suspend fun updateStreak(streakDays: Int)
}
