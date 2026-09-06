package com.example.languagebridge.data

import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class TranslationOutcome {
    data class Success(
        val recognizedText: String,
        val translatedText: String
    ) : TranslationOutcome()

    data class Error(
        val message: String
    ) : TranslationOutcome()
}

class AzureTranslationService(
    private val speechKey: String,
    private val speechRegion: String
) {
    suspend fun recognizeAndTranslate(
        sourceLanguage: String,
        targetLanguageShort: String,
    ): TranslationOutcome = withContext(Dispatchers.IO) {
        val config = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion)
        config.speechSynthesisLanguage = sourceLanguage
        config.addTargetLanguage(targetLanguageShort)

        val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
        val recognizer = TranslationRecognizer(config, audioConfig)

        try {
            val result = recognizer.recognizeOnceAsync().get()
            val translated = result.translations[targetLanguageShort] ?: ""
            TranslationOutcome.Success(result.text, translated)
        } catch (e: Exception) {
            TranslationOutcome.Error(e.message ?: "Unknown error")
        } finally {
            recognizer.close()
            config.close()
        }
    }


    suspend fun speak(
        text: String,
        voiceName: String
    ): Unit = withContext(Dispatchers.IO) {
        val config = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion)
        config.speechSynthesisVoiceName = voiceName
        val synthesizer = SpeechSynthesizer(config)

        try {
            synthesizer.SpeakTextAsync(text).get()
        } finally {
            synthesizer.close()
            config.close()
        }
    }
}