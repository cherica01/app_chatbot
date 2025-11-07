package com.example.chatbotapp.data.api

// REQUÊTE
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

// RÉPONSE
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

// Pour les erreurs
data class GeminiError(
    val error: ErrorDetail?
)

data class ErrorDetail(
    val code: Int,
    val message: String,
    val status: String
)