package com.englishlearning.data.translation

import com.englishlearning.data.model.Definition
import com.englishlearning.data.model.DictionaryResponse
import com.englishlearning.data.model.WordTranslation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 翻译服务 - 使用Free Dictionary API
 * API文档: https://dictionaryapi.dev/
 */
@Singleton
class TranslationService @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/"

    /**
     * 查询单词翻译
     */
    suspend fun translateWord(word: String): Result<WordTranslation> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("TranslationService", "Translating word: $word")

            // 构建URL
            val url = "$baseUrl${word.lowercase().trim()}"

            // 创建HTTP请求
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000 // 10秒
            connection.readTimeout = 10000

            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            android.util.Log.d("TranslationService", "Response code: $responseCode")

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.d("TranslationService", "Response: $response")

                // 解析JSON响应
                val responses: List<DictionaryResponse> = json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(DictionaryResponse.serializer()),
                    response
                )

                if (responses.isNotEmpty()) {
                    val dictResponse = responses[0]
                    val wordTranslation = parseWordTranslation(dictResponse)
                    Result.success(wordTranslation)
                } else {
                    Result.failure(Exception("No translation found"))
                }
            } else {
                val error = connection.errorStream.bufferedReader().use { it.readText() }
                android.util.Log.e("TranslationService", "Error response: $error")
                Result.failure(Exception("HTTP $responseCode: $error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("TranslationService", "Translation failed", e)
            Result.failure(e)
        }
    }

    /**
     * 解析DictionaryResponse为WordTranslation
     */
    private fun parseWordTranslation(response: DictionaryResponse): WordTranslation {
        // 获取音标（优先使用phonetic字段）
        val phonetic = response.phonetic
            ?: response.phonetics?.firstOrNull { !it.text.isNullOrBlank() }?.text

        // 获取发音音频
        val audioUrl = response.phonetics?.firstOrNull { !it.audio.isNullOrBlank() }?.audio

        // 获取第一个词性和释义
        val firstMeaning = response.meanings.firstOrNull()
        val partOfSpeech = firstMeaning?.partOfSpeech
        val firstDefinition = firstMeaning?.definitions?.firstOrNull()

        // 获取例句
        val example = firstDefinition?.example

        // 获取同义词
        val synonyms = firstDefinition?.synonyms

        return WordTranslation(
            word = response.word,
            phonetic = phonetic,
            audioUrl = audioUrl,
            partOfSpeech = partOfSpeech,
            definition = firstDefinition?.definition,
            example = example,
            synonyms = synonyms
        )
    }
}
