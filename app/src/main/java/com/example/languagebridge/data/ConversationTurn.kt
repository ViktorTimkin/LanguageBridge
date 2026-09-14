package com.example.languagebridge.data

data class ConversationTurn(
    val russianText: String,
    val armenianText: String
) {
    fun textFor(language: Language): String =
        if (language == Language.RUSSIAN) russianText else armenianText
}