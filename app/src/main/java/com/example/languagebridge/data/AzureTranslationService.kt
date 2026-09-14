package com.example.languagebridge.data

import com.microsoft.cognitiveservices.speech.PropertyId
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class TranslationOutcome {
    data class Success(val recognizedText: String, val translatedText: String) : TranslationOutcome()
    data class Error(val message: String) : TranslationOutcome()
}

class AzureTranslationService(
    private val speechKey: String,
    private val speechRegion: String
) {
    private var recognizer: TranslationRecognizer? = null
    private var activeConfig: SpeechTranslationConfig? = null
    private var targetLangShort: String = ""

    // Копим сюда все фразы, распознанные, пока кнопка зажата
    private val recognizedBuilder = StringBuilder()
    private val translatedBuilder = StringBuilder()

    // Вызывается по НАЖАТИЮ кнопки — запускает непрерывное прослушивание
    fun startListening(sourceLang: String, targetLangShort: String) {
        this.targetLangShort = targetLangShort
        recognizedBuilder.clear()
        translatedBuilder.clear()

        val config = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion)
        config.speechRecognitionLanguage = sourceLang
        config.addTargetLanguage(targetLangShort)
        config.setProperty(PropertyId.Speech_SegmentationSilenceTimeoutMs, "700")

        val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
        val newRecognizer = TranslationRecognizer(config, audioConfig)

        // Подписываемся на событие "очередная фраза распознана и переведена"
        newRecognizer.recognized.addEventListener { _, event ->
            if (event.result.reason == ResultReason.TranslatedSpeech) {
                val text = event.result.text
                val translated = event.result.translations[targetLangShort] ?: ""
                if (text.isNotBlank()) {
                    if (recognizedBuilder.isNotEmpty()) recognizedBuilder.append(" ")
                    recognizedBuilder.append(text)
                }
                if (translated.isNotBlank()) {
                    if (translatedBuilder.isNotEmpty()) translatedBuilder.append(" ")
                    translatedBuilder.append(translated)
                }
            }
        }

        recognizer = newRecognizer
        activeConfig = config
        newRecognizer.startContinuousRecognitionAsync().get()
    }

    // Вызывается по ОТПУСКАНИЮ кнопки — останавливает и возвращает итог
    suspend fun stopListening(): TranslationOutcome = withContext(Dispatchers.IO) {
        val activeRecognizer = recognizer
            ?: return@withContext TranslationOutcome.Error("Распознавание не было запущено")

        try {
            activeRecognizer.stopContinuousRecognitionAsync().get()
        } catch (e: Exception) {
            return@withContext TranslationOutcome.Error(e.message ?: "Unknown error")
        } finally {
            activeRecognizer.close()
            activeConfig?.close()
            recognizer = null
        }

        if (recognizedBuilder.isEmpty()) {
            TranslationOutcome.Error("Речь не распознана. Попробуйте ещё раз.")
        } else {
            TranslationOutcome.Success(
                recognizedText = recognizedBuilder.toString(),
                translatedText = translatedBuilder.toString()
            )
        }
    }

    suspend fun speak(text: String, voiceName: String): Unit = withContext(Dispatchers.IO) {
        val config = SpeechConfig.fromSubscription(speechKey, speechRegion)
        config.speechSynthesisVoiceName = voiceName
        val audioOutputConfig = AudioConfig.fromDefaultSpeakerOutput()
        val synthesizer = SpeechSynthesizer(config, audioOutputConfig)
        try {
            synthesizer.SpeakTextAsync(text).get()
        } finally {
            synthesizer.close()
            config.close()
        }
    }
}