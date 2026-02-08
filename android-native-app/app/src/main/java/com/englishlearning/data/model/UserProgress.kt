package com.englishlearning.data.model

/**
 * User learning progress tracking
 */
data class UserProgress(
    val currentBookId: String = "book1",
    val currentLessonId: String? = null,
    val totalStudyTime: Long = 0L, // Total study time in milliseconds
    val streakDays: Int = 0,
    val lastStudyDate: Long? = null, // Timestamp of last study session
    val masteredWords: Int = 0,
    val completedQuizzes: Int = 0,
    val quizScoreAverage: Float = 0f
) {
    /**
     * Calculate total study time in hours
     */
    fun getTotalStudyHours(): Float {
        return totalStudyTime / (1000f * 60f * 60f)
    }

    /**
     * Calculate total study time in minutes
     */
    fun getTotalStudyMinutes(): Long {
        return totalStudyTime / (1000L * 60L)
    }

    /**
     * Get formatted study time string
     */
    fun getFormattedStudyTime(): String {
        val hours = getTotalStudyHours().toInt()
        val minutes = (getTotalStudyMinutes() % 60).toInt()
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
