package com.englishlearning.data.repository

import com.englishlearning.data.preferences.UserPreferencesManager
import com.englishlearning.data.remote.AIService
import com.englishlearning.data.remote.model.ChatCompletionRequest
import com.englishlearning.data.remote.model.Message
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    // Dynamic service creation because Base URL can change
    private fun createService(baseUrl: String): AIService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AIService::class.java)
    }

    suspend fun explainSentence(sentence: String): Result<String> {
        val prefs = userPreferencesManager.userPreferences.first()
        val apiKey = prefs.aiApiKey
        val baseUrl = prefs.aiBaseUrl
        val model = prefs.aiModel

        if (apiKey.isBlank()) {
            return Result.failure(Exception("API Key not set. Please configure in Settings."))
        }

        // Validate Base URL
        val validBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return try {
            val service = createService(validBaseUrl)
            val prompt = "Please explain the grammar and meaning of this English sentence in simple Chinese: \"$sentence\""
            
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(
                    Message("system", "You are a helpful English learning assistant."),
                    Message("user", prompt)
                )
            )

            val response = service.createChatCompletion("Bearer $apiKey", request)
            val content = response.choices.firstOrNull()?.message?.content
            
            if (content != null) {
                Result.success(content)
            } else {
                Result.failure(Exception("Empty response from AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
