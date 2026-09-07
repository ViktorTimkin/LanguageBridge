package com.example.languagebridge.data

object LanguageMap {
    fun speechLocale(displayName: String): String = when (displayName) {
        "Русский" -> "ru-RU"
        "Հայերեն" -> "hy-AM"
        else -> "ru-RU"
    }

    fun translationCode(displayName: String): String = when (displayName) {
        "Русский" -> "ru"
        "Հայերեն" -> "hy"
        else -> "ru"
    }

    fun voiceName(displayName: String): String = when (displayName) {
        "Русский" -> "ru-RU-SvetlanaNeural"
        "Հայերեն" -> "hy-AM-AnahitNeural"
        else -> "ru-RU-SvetlanaNeural"
    }
}