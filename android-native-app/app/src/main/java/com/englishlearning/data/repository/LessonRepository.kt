package com.englishlearning.data.repository

import com.englishlearning.data.local.dao.LessonProgressDao
import com.englishlearning.data.local.dao.VocabularyProgressDao
import com.englishlearning.data.local.database.LessonProgressEntity
import com.englishlearning.data.local.database.VocabularyProgressEntity
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.model.LessonProgress
import com.englishlearning.data.model.Vocabulary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for lesson progress and vocabulary management
 */
@Singleton
class LessonRepository @Inject constructor(
    private val lessonProgressDao: LessonProgressDao,
    private val vocabularyProgressDao: VocabularyProgressDao
) {
    /**
     * Get progress for a specific lesson
     */
    suspend fun getLessonProgress(lessonId: String): LessonProgress? = withContext(Dispatchers.IO) {
        val entity = lessonProgressDao.getProgress(lessonId)
        entity?.let {
            when (it.progress) {
                "NOT_STARTED" -> LessonProgress.NOT_STARTED
                "IN_PROGRESS" -> LessonProgress.IN_PROGRESS
                "COMPLETED" -> LessonProgress.COMPLETED
                else -> LessonProgress.NOT_STARTED
            }
        }
    }

    /**
     * Update lesson progress after study session
     */
    suspend fun updateLessonProgress(
        lessonId: String,
        bookId: String,
        lastPosition: Long,
        completionPercentage: Float,
        timeSpent: Long,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        val progress = when {
            isCompleted -> "COMPLETED"
            completionPercentage > 0 -> "IN_PROGRESS"
            else -> "NOT_STARTED"
        }

        lessonProgressDao.updateStudyProgress(
            lessonId = lessonId,
            progress = progress,
            lastPosition = lastPosition,
            completionPercentage = completionPercentage,
            timeSpent = timeSpent,
            lastStudyDate = System.currentTimeMillis()
        )
    }

    /**
     * Initialize progress for a new lesson
     */
    suspend fun initializeLessonProgress(lessonId: String, bookId: String) = withContext(Dispatchers.IO) {
        val existing = lessonProgressDao.getProgress(lessonId)
        if (existing == null) {
            lessonProgressDao.insertProgress(
                LessonProgressEntity(
                    lessonId = lessonId,
                    bookId = bookId,
                    progress = "NOT_STARTED",
                    lastPosition = 0L,
                    completionPercentage = 0f,
                    totalTimeSpent = 0L,
                    lastStudyDate = null
                )
            )
        }
    }

    /**
     * Save vocabulary words from a lesson
     */
    suspend fun saveVocabulary(lessonId: String, vocabularyList: List<Vocabulary>) = withContext(Dispatchers.IO) {
        vocabularyList.forEach { vocab ->
            val existing = vocabularyProgressDao.getWordProgress(vocab.word)
            if (existing == null) {
                vocabularyProgressDao.insertWord(
                    VocabularyProgressEntity(
                        word = vocab.word,
                        lessonId = lessonId,
                        isMastered = false,
                        reviewCount = 0,
                        lastReviewDate = null,
                        correctCount = 0,
                        incorrectCount = 0
                    )
                )
            }
        }
    }

    /**
     * Update vocabulary after review
     */
    suspend fun updateVocabularyReview(
        word: String,
        isCorrect: Boolean
    ) = withContext(Dispatchers.IO) {
        val current = vocabularyProgressDao.getWordProgress(word) ?: return@withContext

        val mastered = current.reviewCount >= 5 && (current.correctCount.toFloat() / (current.correctCount + current.incorrectCount)) >= 0.8f

        vocabularyProgressDao.updateReviewResult(
            word = word,
            mastered = mastered,
            lastReviewDate = System.currentTimeMillis(),
            correctIncrement = if (isCorrect) 1 else 0,
            incorrectIncrement = if (isCorrect) 0 else 1
        )
    }

    /**
     * Get words due for review
     */
    suspend fun getWordsForReview(limit: Int = 20): List<VocabularyProgressEntity> = withContext(Dispatchers.IO) {
        vocabularyProgressDao.getWordsForReview(limit)
    }

    /**
     * Get all vocabulary progress
     */
    suspend fun getAllVocabulary(): List<VocabularyProgressEntity> = withContext(Dispatchers.IO) {
        vocabularyProgressDao.getAllWords()
    }

    /**
     * Get all lesson progress
     */
    suspend fun getAllLessonProgress(): List<LessonProgressEntity> = withContext(Dispatchers.IO) {
        lessonProgressDao.getAllProgress()
    }
}
