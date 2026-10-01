package com.example.languagebridge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.languagebridge.data.AzureTranslationService

class TranslatorViewModelFactory(
    private val service: AzureTranslationService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TranslatorViewModel(service) as T
    }
}