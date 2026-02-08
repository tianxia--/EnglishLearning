package com.englishlearning.di

import android.content.Context
import androidx.room.Room
import com.englishlearning.data.local.dao.LessonProgressDao
import com.englishlearning.data.local.dao.SpeakingDao
import com.englishlearning.data.local.dao.UserProgressDao
import com.englishlearning.data.local.dao.VocabularyProgressDao
import com.englishlearning.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "new_concept_english_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLessonProgressDao(database: AppDatabase): LessonProgressDao {
        return database.lessonProgressDao()
    }

    @Provides
    fun provideUserProgressDao(database: AppDatabase): UserProgressDao {
        return database.userProgressDao()
    }

    @Provides
    fun provideVocabularyProgressDao(database: AppDatabase): VocabularyProgressDao {
        return database.vocabularyProgressDao()
    }

    @Provides
    fun provideSpeakingDao(database: AppDatabase): SpeakingDao {
        return database.speakingDao()
    }
}
