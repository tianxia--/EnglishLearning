package com.englishlearning.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.englishlearning.data.local.dao.*
import com.englishlearning.data.local.dao.SpeakingDao

/**
 * Room database for New Concept English app
 */
@Database(
    entities = [
        LessonProgressEntity::class,
        UserProgressEntity::class,
        VocabularyProgressEntity::class,
        SpeakingRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun vocabularyProgressDao(): VocabularyProgressDao
    abstract fun speakingDao(): SpeakingDao
}
