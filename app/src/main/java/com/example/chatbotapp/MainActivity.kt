package com.example.chatbotapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatbotapp.data.api.ApiClient
import com.example.chatbotapp.data.repository.ChatRepository
import com.example.chatbotapp.stt.VoiceToText
import com.example.chatbotapp.tts.TextToSpeechHelper
import com.example.chatbotapp.ui.ChatScreen
import com.example.chatbotapp.ui.ChatViewModel
import com.example.chatbotapp.ui.ChatViewModelFactory
import com.example.chatbotapp.ui.theme.ChatBotAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = "AIzaSyBKH3d36RMbBXQ97ul8OAW_YG-VaPr7c-0" // Remplacez par votre clé API Gemini

        val service = ApiClient.create()
        val repo = ChatRepository(api = service)
        val tts = TextToSpeechHelper(this)
        val voiceToText = VoiceToText(this)

        val viewModelFactory = ChatViewModelFactory(repo, apiKey, tts, voiceToText)

        setContent {
            ChatBotAppTheme {
                val vm: ChatViewModel = viewModel(factory = viewModelFactory)
                ChatScreen(vm = vm)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Nettoyage pour éviter les fuites de mémoire
        // Le ViewModel s'en charge normalement, mais c'est une sécurité
    }
}