package com.englishlearning.data.repository

import android.content.Context
import com.englishlearning.data.model.Book
import com.englishlearning.data.model.BookIndex
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.model.LessonsIndex
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for loading and managing lesson content with caching
 */
@Singleton
class ContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonLoader: LessonLoader
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cache for books list
    private var booksCache: List<Book>? = null

    /**
     * Load all books from assets (with caching)
     */
    suspend fun loadAllBooks(): Result<List<Book>> = withContext(Dispatchers.IO) {
        // Return cached data if available
        booksCache?.let {
            android.util.Log.d("ContentRepository", "Returning cached books list")
            return@withContext Result.success(it)
        }

        try {
            android.util.Log.d("ContentRepository", "Starting to load indexed_lessons.json")
            val jsonString = context.assets.open("indexed_lessons.json")
                .bufferedReader()
                .use { it.readText() }

            android.util.Log.d("ContentRepository", "JSON loaded, length: ${jsonString.length}")

            val index = json.decodeFromString<LessonsIndex>(jsonString)
            android.util.Log.d("ContentRepository", "Found ${index.books.size} books in JSON")

            val books = index.books.map { bookIndex ->
                val book = Book(
                    id = bookIndex.id,
                    title = bookIndex.title,
                    level = bookIndex.level,
                    description = bookIndex.description,
                    audioPath = bookIndex.audioPath,
                    pdfPath = bookIndex.pdfPath,
                    lessonCount = bookIndex.lessonCount,
                    lessons = emptyList(), // Lessons will be loaded on demand
                    completedLessons = 0,
                    totalTimeSpent = 0L
                )
                android.util.Log.d("ContentRepository", "Parsed book: ${book.id} - ${book.title}")
                book
            }

            android.util.Log.d("ContentRepository", "Successfully loaded ${books.size} books")

            // Cache the result
            booksCache = books
            Result.success(books)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepository", "Error loading books", e)
            Result.failure(e)
        }
    }

    /**
     * Load a specific book by ID
     */
    suspend fun loadBook(bookId: String): Result<Book?> = withContext(Dispatchers.IO) {
        try {
            val allBooksResult = loadAllBooks()
            if (allBooksResult.isSuccess) {
                val book = allBooksResult.getOrNull()?.find { it.id == bookId }
                Result.success(book)
            } else {
                Result.failure(allBooksResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load a specific lesson
     */
    suspend fun loadLesson(bookId: String, lessonId: String): Result<Lesson?> = withContext(Dispatchers.IO) {
        lessonLoader.loadLesson(bookId, lessonId)
    }

    /**
     * Load all lessons for a book
     */
    suspend fun loadBookLessons(bookId: String): Result<List<Lesson>> = withContext(Dispatchers.IO) {
        lessonLoader.loadBookLessons(bookId)
    }

    companion object {
        const val ASSETS_BASE_PATH = "indexed_lessons.json"
    }
}
