package com.englishlearning.data.remote

import com.englishlearning.data.remote.model.ChatCompletionRequest
import com.englishlearning.data.remote.model.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for OpenAI-compatible Chat Completion API
 */
interface AIService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
