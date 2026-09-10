package com.example.languagebridge.data

import com.microsoft.cognitiveservices.speech.CancellationDetails
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

    suspend fun recognizeAndTranslate(
        sourceLang: String,
        targetLangShort: String
    ): TranslationOutcome = withContext(Dispatchers.IO) {
        val config = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion)
        config.speechRecognitionLanguage = sourceLang
        config.addTargetLanguage(targetLangShort)

        // Даём больше времени на паузы внутри фразы, прежде чем считать её законченной
        config.setProperty(
            com.microsoft.cognitiveservices.speech.PropertyId.Speech_SegmentationSilenceTimeoutMs,
            "1500" // было по умолчанию гораздо меньше
        )
        // Сколько ждать, прежде чем человек вообще начнёт говорить
        config.setProperty(
            com.microsoft.cognitiveservices.speech.PropertyId.SpeechServiceConnection_InitialSilenceTimeoutMs,
            "10000"
        )
        val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
        val recognizer = TranslationRecognizer(config, audioConfig)

        try {
            val result = recognizer.recognizeOnceAsync().get()

            when (result.reason) {
                ResultReason.TranslatedSpeech -> {
                    val translated = result.translations[targetLangShort] ?: ""
                    TranslationOutcome.Success(result.text, translated)
                }
                ResultReason.RecognizedSpeech -> {
                    // Речь распознана, но перевод почему-то не пришёл
                    TranslationOutcome.Error("Перевод не получен для языка $targetLangShort")
                }
                ResultReason.NoMatch -> {
                    TranslationOutcome.Error("Речь не распознана. Попробуйте говорить чётче и ближе к микрофону.")
                }
                ResultReason.Canceled -> {
                    val cancellation = CancellationDetails.fromResult(result)
                    TranslationOutcome.Error(
                        "Отменено: ${cancellation.reason}. ${cancellation.errorDetails}"
                    )
                }
                else -> TranslationOutcome.Error("Неожиданный результат: ${result.reason}")
            }
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
        val config = SpeechConfig.fromSubscription(speechKey, speechRegion)
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