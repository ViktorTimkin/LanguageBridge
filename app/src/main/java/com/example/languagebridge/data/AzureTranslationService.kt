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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
sealed class TranslationOutcome {
    data class Success(val recognizedText: String, val translatedText: String) : TranslationOutcome()
    data class Error(val message: String) : TranslationOutcome()
}

class AzureTranslationService(
    private val speechKey: String,
    private val speechRegion: String,
    private val translatorKey: String,
) {
    private var recognizer: TranslationRecognizer? = null
    private var activeConfig: SpeechTranslationConfig? = null
    private var targetLangShort: String = ""

    private val recognizedBuilder = StringBuilder()
    private val translatedBuilder = StringBuilder()

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

    suspend fun stopListening(): TranslationOutcome? = withContext(Dispatchers.IO) {
        val activeRecognizer = recognizer ?: return@withContext null

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
                translatedText = translatedBuilder.toString(),
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
    suspend fun translateText(
        text: String,
        sourceLangShort: String,
        targetLangShort: String,
    ): TranslationOutcome = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "https://api.cognitive.microsofttranslator.com/translate" +
                        "?api-version=3.0&from=$sourceLangShort&to=$targetLangShort"
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", translatorKey)
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true

            val requestBody = JSONArray().apply {
                put(JSONObject().apply { put("Text", text) })
            }.toString()

            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val translatedText = JSONArray(response)
                    .getJSONObject(0)
                    .getJSONArray("translations")
                    .getJSONObject(0)
                    .getString("text")
                TranslationOutcome.Success(
                    recognizedText = text,
                    translatedText = translatedText,
                )
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "HTTP ${connection.responseCode}"
                TranslationOutcome.Error(error)
            }
        } catch (e: Exception) {
            TranslationOutcome.Error(e.message ?: "Unknown error")
        }
    }
}