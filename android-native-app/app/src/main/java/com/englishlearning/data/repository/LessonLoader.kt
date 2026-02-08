package com.englishlearning.data.repository

import android.content.Context
import com.englishlearning.data.model.Lesson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to load lessons from assets
 */
@Singleton
class LessonLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Load all lessons for a specific book from assets
     */
    suspend fun loadBookLessons(bookId: String): Result<List<Lesson>> = withContext(Dispatchers.IO) {
        try {
            val lessons = mutableListOf<Lesson>()

            // List all files in the book directory
            val bookPath = "$bookId/"
            val files = context.assets.list(bookId) ?: emptyArray()

            files
                .filter { it.endsWith(".json") }
                .sorted()
                .forEach { fileName ->
                    try {
                        val jsonString = context.assets.open("$bookPath$fileName")
                            .bufferedReader()
                            .use { it.readText() }

                        val lesson = json.decodeFromString<Lesson>(jsonString)
                        lessons.add(lesson)
                    } catch (e: Exception) {
                        // Skip invalid files
                        println("Error loading lesson $fileName: ${e.message}")
                    }
                }

            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load a single lesson by ID
     */
    suspend fun loadLesson(bookId: String, lessonId: String): Result<Lesson?> = withContext(Dispatchers.IO) {
        try {
            val lessonPath = "$bookId/$lessonId.json"
            val jsonString = context.assets.open(lessonPath)
                .bufferedReader()
                .use { it.readText() }

            val lesson = json.decodeFromString<Lesson>(jsonString)
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
