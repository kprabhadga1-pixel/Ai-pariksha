package com.example.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceHelper(
    private val context: Context,
    private val onSpeechResults: (String) -> Unit,
    private val onSpeechStateChanged: (SpeechState) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false

    enum class SpeechState {
        IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
    }

    init {
        // Initialize TTS with Indian English Locale for cultural relevance
        tts = TextToSpeech(context, this)

        // Initialize Speech Recognizer
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceHelper)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("en", "IN")) // Indian English accent
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US // Fallback
            }
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStateChanged(SpeechState.SPEAKING)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeechStateChanged(SpeechState.IDLE)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeechStateChanged(SpeechState.IDLE)
                }
            })
            isTtsInitialized = true
        } else {
            onSpeechStateChanged(SpeechState.ERROR)
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            stopListening()
            onSpeechStateChanged(SpeechState.SPEAKING)
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "GuruJiSpeech")
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "GuruJiSpeech")
        }
    }

    fun stopSpeaking() {
        if (isTtsInitialized) {
            tts?.stop()
            onSpeechStateChanged(SpeechState.IDLE)
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            onSpeechStateChanged(SpeechState.ERROR)
            return
        }
        stopSpeaking()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Guru Ji your doubt...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
            onSpeechStateChanged(SpeechState.LISTENING)
        } catch (e: Exception) {
            onSpeechStateChanged(SpeechState.ERROR)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun destroy() {
        try {
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore
        }
    }

    // --- SpeechRecognizer Callbacks ---

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        onSpeechStateChanged(SpeechState.PROCESSING)
    }

    override fun onError(error: Int) {
        onSpeechStateChanged(SpeechState.IDLE)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onSpeechResults(matches[0])
        } else {
            onSpeechStateChanged(SpeechState.IDLE)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
