package com.example.chatbotapp.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") key: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}