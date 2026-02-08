package com.englishlearning.domain

import com.englishlearning.data.model.Book
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.repository.LessonRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskGenerator @Inject constructor(
    private val contentRepository: ContentRepository,
    private val lessonRepository: LessonRepository
) {

    /**
     * Generate a list of recommended tasks for the user
     */
    suspend fun generateDailyTasks(): List<DailyTask> {
        val tasks = mutableListOf<DailyTask>()
        val books = contentRepository.loadAllBooks().getOrNull() ?: return emptyList()

        // 1. Find Main Learning Task (Next Lesson)
        val nextLessonTask = findNextLessonTask(books)
        if (nextLessonTask != null) {
            tasks.add(nextLessonTask)
        } else {
             // If all done, maybe Suggest Reviewing Book 1?
             // For now, default to Book 1 Lesson 1 if nothing found (should be impossible if books exist)
             if (books.isNotEmpty()) {
                  val book1 = books[0]
                  contentRepository.loadBookLessons(book1.id).getOrNull()?.firstOrNull()?.let { lesson ->
                       tasks.add(DailyTask.Learn(lesson, book1, "Start your journey!"))
                  }
             }
        }

        // 2. Vocabulary Review Task
        // Check if there are words to review
        // Mocking logic: just always show "Review Vocabulary" if they have learned some words
        // In real impl, check vocabulary DAO count
        val vocabCount = lessonRepository.getAllVocabulary().count()
        if (vocabCount > 0) {
            tasks.add(DailyTask.ReviewVocabulary(count = 10)) // Mock count or query actual due count
        }

        // 3. Speaking Task 
        // Always good to suggest speaking
        tasks.add(DailyTask.SpeakingChallenge)

        return tasks
    }

    private suspend fun findNextLessonTask(books: List<Book>): DailyTask? {
        // Iterate books in order
        for (book in books) {
            val lessons = contentRepository.loadBookLessons(book.id).getOrNull() ?: continue
            val allProgress = lessonRepository.getAllLessonProgress()
            
            // Find first uncompleted lesson
            for (lesson in lessons) {
                val progress = allProgress.find { it.lessonId == lesson.id }
                if (progress == null || progress.progress != "COMPLETED") {
                    // Found the next lesson!
                    val isStarted = progress != null && progress.progress == "IN_PROGRESS"
                    return DailyTask.Learn(
                        lesson = lesson,
                        book = book,
                        description = if (isStarted) "Continue learning" else "Start new lesson"
                    )
                }
            }
        }
        return null // All books completed!
    }
}

sealed class DailyTask {
    data class Learn(
        val lesson: Lesson, 
        val book: Book,
        val description: String
    ) : DailyTask()

    data class ReviewVocabulary(val count: Int) : DailyTask()
    
    object SpeakingChallenge : DailyTask()
}
