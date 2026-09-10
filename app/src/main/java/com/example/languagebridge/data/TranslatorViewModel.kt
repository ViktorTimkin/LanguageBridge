package com.example.languagebridge.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val service: AzureTranslationService
) : ViewModel() {

    val conversation = mutableStateListOf<ConversationTurn>()
    var isBusy by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun startTranslation(sourceLanguage: Language, targetLanguage: Language) {
        isBusy = true
        errorMessage = null

        viewModelScope.launch {
            when (val outcome = service.recognizeAndTranslate(
                sourceLang = sourceLanguage.speechLocale,
                targetLangShort = targetLanguage.translationCode
            )) {
                is TranslationOutcome.Success -> {
                    val turn = if (sourceLanguage == Language.RUSSIAN) {
                        ConversationTurn(
                            russianText = outcome.recognizedText,
                            armenianText = outcome.translatedText
                        )
                    } else {
                        ConversationTurn(
                            russianText = outcome.translatedText,
                            armenianText = outcome.recognizedText
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
}