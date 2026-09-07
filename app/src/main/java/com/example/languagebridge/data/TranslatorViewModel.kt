package com.example.languagebridge.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val service: AzureTranslationService
) : ViewModel() {

    var recognizedText by mutableStateOf("")
        private set
    var translatedText by mutableStateOf("")
        private set
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
                    recognizedText = outcome.recognizedText
                    translatedText = outcome.translatedText
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