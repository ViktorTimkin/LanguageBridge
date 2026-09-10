package com.example.languagebridge.data

enum class Language(
    val displayName: String,
    val speechLocale: String,       // код для распознавания речи, напр. "ru-RU"
    val translationCode: String,    // короткий код для перевода, напр. "ru"
    val voiceName: String           // голос для озвучки
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