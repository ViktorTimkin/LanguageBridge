package com.example.languagebridge.data

enum class Language(
    val displayName: String,
    val speechLocale: String,
    val translationCode: String,
    val voiceName: String
) {
    RUSSIAN(
        displayName = "Русский",
        speechLocale = "ru-RU",
        translationCode = "ru",
        voiceName = "ru-RU-SvetlanaNeural"
    ),
    ARMENIAN(
        displayName = "Հայերեն",
        speechLocale = "hy-AM",
        translationCode = "hy",
        voiceName = "hy-AM-AnahitNeural"
    );

    fun other(): Language = if (this == RUSSIAN) ARMENIAN else RUSSIAN
}