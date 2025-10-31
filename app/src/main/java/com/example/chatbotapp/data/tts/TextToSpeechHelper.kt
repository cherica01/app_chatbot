package com.example.chatbotapp.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onInitListener: ((Boolean) -> Unit)? = null
    private var onUtteranceComplete: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.FRENCH)
            isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED

            if (isInitialized) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        onUtteranceComplete?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        onUtteranceComplete?.invoke()
                    }
                })
            }
        } else {
            isInitialized = false
            Log.e("TTS", "Initialization Failed!")
        }
        onInitListener?.invoke(isInitialized)
    }

    fun setOnInitListener(listener: (Boolean) -> Unit) {
        onInitListener = listener
    }

    fun setOnUtteranceCompleteListener(listener: () -> Unit) {
        onUtteranceComplete = listener
    }

    fun speak(text: String, utteranceId: String = "utteranceId") {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

    fun isInitialized(): Boolean = isInitialized
}