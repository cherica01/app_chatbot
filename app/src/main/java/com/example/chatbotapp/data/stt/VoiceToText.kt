package com.example.chatbotapp.stt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceToText(private val activity: Activity) {
    private var recognizer: SpeechRecognizer? = null
    private var isListening = false

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStart: () -> Unit = {},
        onEnd: () -> Unit = {}
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            onError("Reconnaissance vocale non disponible")
            return
        }

        if (isListening) {
            stopListening()
            return
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    onStart()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    onEnd()
                }

                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    onResult(text)
                    isListening = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    onResult(text)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Erreur audio"
                        SpeechRecognizer.ERROR_CLIENT -> "Erreur client"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions insuffisantes"
                        SpeechRecognizer.ERROR_NETWORK -> "Erreur réseau"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout réseau"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Aucune correspondance trouvée"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconnaissance vocale occupée"
                        SpeechRecognizer.ERROR_SERVER -> "Erreur serveur"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout de parole"
                        else -> "Erreur inconnue: $error"
                    }
                    onError(errorMessage)
                    isListening = false
                }
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            startListening(intent)
        }
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        isListening = false
        recognizer = null
    }

    fun isListening(): Boolean = isListening
}