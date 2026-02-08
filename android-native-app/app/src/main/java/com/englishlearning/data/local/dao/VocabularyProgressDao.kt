package com.englishlearning.data.local.dao

import androidx.room.*
import com.englishlearning.data.local.database.VocabularyProgressEntity

/**
 * DAO for vocabulary progress operations
 */
@Dao
interface VocabularyProgressDao {
    @Query("SELECT * FROM vocabulary_progress WHERE word = :word")
    suspend fun getWordProgress(word: String): VocabularyProgressEntity?

    @Query("SELECT * FROM vocabulary_progress WHERE lessonId = :lessonId")
    suspend fun getLessonWords(lessonId: String): List<VocabularyProgressEntity>

    @Query("SELECT * FROM vocabulary_progress WHERE isMastered = 0 ORDER BY lastReviewDate ASC LIMIT :limit")
    suspend fun getWordsForReview(limit: Int = 20): List<VocabularyProgressEntity>

    @Query("SELECT * FROM vocabulary_progress ORDER BY lastReviewDate DESC")
    suspend fun getAllWords(): List<VocabularyProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyProgressEntity)

    @Update
    suspend fun updateWord(word: VocabularyProgressEntity)

    @Query("UPDATE vocabulary_progress SET isMastered = :mastered, reviewCount = reviewCount + 1, lastReviewDate = :lastReviewDate, correctCount = correctCount + :correctIncrement, incorrectCount = incorrectCount + :incorrectIncrement WHERE word = :word")
    suspend fun updateReviewResult(
        word: String,
        mastered: Boolean,
        lastReviewDate: Long,
        correctIncrement: Int,
        incorrectIncrement: Int
    )

    @Query("DELETE FROM vocabulary_progress WHERE word = :word")
    suspend fun deleteWord(word: String)
}
