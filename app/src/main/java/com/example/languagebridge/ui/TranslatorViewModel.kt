package com.example.languagebridge.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagebridge.data.AzureTranslationService
import com.example.languagebridge.data.ConversationTurn
import com.example.languagebridge.data.Language
import com.example.languagebridge.data.TranslationOutcome
import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val service: AzureTranslationService,
) : ViewModel() {

    val conversation = mutableStateListOf<ConversationTurn>()

    var isBusy by mutableStateOf(value = false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun startListening(sourceLanguage: Language) {
        errorMessage = null
        isBusy = true
        service.startListening(
            sourceLang = sourceLanguage.speechLocale,
            targetLangShort = sourceLanguage.other().translationCode,
        )
    }

    fun stopListening(sourceLanguage: Language) {
        val targetLanguage = sourceLanguage.other()

        viewModelScope.launch {
            val outcome = service.stopListening() ?: run {
                isBusy = false
                return@launch
            }
            when (outcome) {
                is TranslationOutcome.Success -> {
                    val turn = if (sourceLanguage == Language.RUSSIAN) {
                        ConversationTurn(
                            russianText = outcome.recognizedText,
                            armenianText = outcome.translatedText,
                        )
                    } else {
                        ConversationTurn(
                            russianText = outcome.translatedText,
                            armenianText = outcome.recognizedText,
                        )
                    }
                    conversation.add(turn)
                    isBusy = false

                    try {
                        service.speak(outcome.translatedText, targetLanguage.voiceName)
                    } catch (e: Exception) {
                        errorMessage = "Ошибка озвучки: ${e.message}"
                    }
                }
                is TranslationOutcome.Error -> {
                    errorMessage = outcome.message
                    isBusy = false
                }
            }
        }
    }
    fun translateTyped(sourceLanguage: Language, text: String) {
        if (text.isBlank()) return

        errorMessage = null
        isBusy = true
        val targetLanguage = sourceLanguage.other()

        viewModelScope.launch {
            when (val outcome = service.translateText(
                text = text,
                sourceLangShort = sourceLanguage.translationCode,
                targetLangShort = targetLanguage.translationCode,
            )) {
                is TranslationOutcome.Success -> {
                    val turn = if (sourceLanguage == Language.RUSSIAN) {
                        ConversationTurn(
                            russianText = outcome.recognizedText,
                            armenianText = outcome.translatedText,
                        )
                    } else {
                        ConversationTurn(
                            russianText = outcome.translatedText,
                            armenianText = outcome.recognizedText,
                        )
                    }
                    conversation.add(turn)
                    isBusy = false

                    try {
                        service.speak(outcome.translatedText, targetLanguage.voiceName)
                    } catch (e: Exception) {
                        errorMessage = "Ошибка озвучки: ${e.message}"
                    }
                }
                is TranslationOutcome.Error -> {
                    errorMessage = outcome.message
                    isBusy = false
                }
            }
        }
    }

    fun speakTurn(turn: ConversationTurn, language: Language) {
        val text = turn.textFor(language)
        viewModelScope.launch {
            try {
                service.speak(text, language.voiceName)
            } catch (e: Exception) {
                errorMessage = "Ошибка озвучки: ${e.message}"
            }
        }
    }
}