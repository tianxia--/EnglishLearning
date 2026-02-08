package com.englishlearning.data.model

import kotlinx.serialization.Serializable

/**
 * Data model for the lessons index file
 */
@Serializable
data class LessonsIndex(
    val version: String,
    val title: String,
    val description: String,
    val lastUpdated: String,
    val books: List<BookIndex>
)

/**
 * Simplified book data for index (without lessons list)
 */
@Serializable
data class BookIndex(
    val id: String,
    val title: String,
    val level: String,
    val description: String,
    val audioPath: String,
    val pdfPath: String,
    val lessonCount: Int
)
