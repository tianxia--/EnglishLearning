package com.englishlearning.data.model

import kotlinx.serialization.Serializable

/**
 * Free Dictionary API 返回的数据模型
 * API: https://api.dictionaryapi.dev/api/v2/entries/en/<word>
 */

@Serializable
data class DictionaryResponse(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<Phonetic>? = null,
    val meanings: List<Meaning>
)

@Serializable
data class Phonetic(
    val text: String? = null,
    val audio: String? = null,
    val sourceUrl: String? = null
)

@Serializable
data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

@Serializable
data class Definition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String>? = null,
    val antonyms: List<String>? = null
)

/**
 * 简化的单词信息，用于UI显示
 */
data class WordTranslation(
    val word: String,
    val phonetic: String? = null,
    val audioUrl: String? = null,
    val partOfSpeech: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val synonyms: List<String>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
