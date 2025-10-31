package com.example.chatbotapp.data.repository

import com.example.chatbotapp.data.api.*
import retrofit2.Response
import java.io.IOException

class ChatRepository(private val api: GeminiService) {
    suspend fun ask(question: String, apiKey: String): String {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = question))
                    )
                )
            )

            val response: Response<GeminiResponse> = api.generateContent(
                key = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Désolé, aucune réponse reçue."
            } else {
                // Tentative de lire l'erreur de l'API
                val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                "Erreur API (${response.code()}): $errorBody"
            }

        } catch (e: IOException) {
            "Erreur de connexion: Vérifiez votre internet - ${e.message}"
        } catch (e: Exception) {
            "Erreur inattendue: ${e.localizedMessage}"
        }
    }
}