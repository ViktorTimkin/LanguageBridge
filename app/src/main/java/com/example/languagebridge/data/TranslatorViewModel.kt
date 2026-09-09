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

    fun startTranslation(sourceLanguage: String, targetLanguage: String) {
        isBusy = true
        errorMessage = null

        val sourceLocal = LanguageMap.speechLocale(sourceLanguage)
        val targetLanguage = LanguageMap.translationCode(targetLanguage)
        val targetVoice = LanguageMap.voiceName(targetLanguage)

        viewModelScope.launch {
            when (val outcome = service.recognizeAndTranslate(sourceLocal, targetLanguage)) {
                is TranslationOutcome.Success -> {
                    val turn = if (sourceLanguage == "Русский") {
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
                    service.speak(outcome.translatedText, targetVoice)
                }

                is TranslationOutcome.Error -> {
                    errorMessage = outcome.message
                    isBusy = false
                }
            }
        }

    }
}