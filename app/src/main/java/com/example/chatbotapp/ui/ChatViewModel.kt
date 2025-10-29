package com.example.chatbotapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatbotapp.data.repository.ChatRepository
import com.example.chatbotapp.stt.VoiceToText
import com.example.chatbotapp.tts.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val text: String)

class ChatViewModel(
    private val repo: ChatRepository,
    private val apiKey: String,
    private val tts: TextToSpeechHelper,
    private val voiceToText: VoiceToText
) : ViewModel() {
    private val _messages = MutableStateFlow(listOf<ChatMessage>())
    val messages = _messages.asStateFlow()

    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _ttsSpeaking = MutableStateFlow(false)
    val ttsSpeaking = _ttsSpeaking.asStateFlow()

    init {
        tts.setOnInitListener { initialized ->
            if (!initialized) {
                // Gérer l'erreur d'initialisation TTS
                addMessage("🤖", "Erreur d'initialisation de la synthèse vocale")
            }
        }

        tts.setOnUtteranceCompleteListener {
            _ttsSpeaking.value = false
        }
    }

    fun onInputChange(s: String) {
        _input.value = s
    }

    fun send() {
        val prompt = _input.value.trim()
        if (prompt.isEmpty()) return

        _input.value = ""
        addMessage("👤", prompt)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val reply = repo.ask(prompt, apiKey)
                addMessage("🤖", reply)
                speak(reply)
            } catch (e: Exception) {
                addMessage("🤖", "Erreur: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startListening() {
        voiceToText.startListening(
            onResult = { text ->
                _input.value = text
            },
            onError = { error ->
                addMessage("🤖", "Erreur reconnaissance vocale: $error")
                _isListening.value = false
            },
            onStart = {
                _isListening.value = true
            },
            onEnd = {
                _isListening.value = false
            }
        )
    }

    fun stopListening() {
        voiceToText.stopListening()
        _isListening.value = false
    }

    fun speak(text: String) {
        if (tts.isInitialized()) {
            _ttsSpeaking.value = true
            tts.speak(text)
        }
    }

    fun stopSpeaking() {
        tts.stop()
        _ttsSpeaking.value = false
    }

    private fun addMessage(role: String, text: String) {
        _messages.value = _messages.value + ChatMessage(role, text)
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
        voiceToText.stopListening()
    }
}

// Factory pour créer le ViewModel avec des paramètres
class ChatViewModelFactory(
    private val repo: ChatRepository,
    private val apiKey: String,
    private val tts: TextToSpeechHelper,
    private val voiceToText: VoiceToText
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repo, apiKey, tts, voiceToText) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}