package com.example.languagebridge.data

import java.util.Locale

object ArmenianTransliterator {
    private val map = mapOf(
        'ա' to "а", 'բ' to "б", 'գ' to "г", 'դ' to "д", 'ե' to "е",
        'զ' to "з", 'է' to "э", 'ը' to "э", 'թ' to "т", 'ժ' to "ж",
        'ի' to "и", 'լ' to "л", 'խ' to "х", 'ծ' to "тц", 'կ' to "к",
        'հ' to "х", 'ձ' to "дз", 'ղ' to "гх", 'ճ' to "тч", 'մ' to "м",
        'յ' to "й", 'ն' to "н", 'շ' to "ш", 'չ' to "ч", 'պ' to "п",
        'ջ' to "дж", 'ռ' to "р", 'ս' to "с", 'վ' to "в", 'տ' to "т",
        'ր' to "р", 'ց' to "ц", 'փ' to "п", 'ք' to "к", 'օ' to "о",
        'ֆ' to "ф", 'և' to "ев"
    )

    fun transliterate(text: String): String {
        val result = StringBuilder()
        var i = 0
        val lowerText = text.lowercase(Locale.getDefault())

        while (i < text.length) {
            val char = lowerText[i]
            val isUpper = text[i].isUpperCase()

            // Handle digrams
            if (i + 1 < text.length) {
                val nextChar = lowerText[i + 1]
                val digram = "$char$nextChar"
                if (digram == "ու") {
                    append(result, "у", isUpper)
                    i += 2
                    continue
                }
            }

            // Handle contextual cases
            when (char) {
                'ե' -> {
                    val isStart = i == 0 || !lowerText[i - 1].isLetter()
                    append(result, if (isStart) "йе" else "е", isUpper)
                }
                'ո' -> {
                    val isStart = i == 0 || !lowerText[i - 1].isLetter()
                    append(result, if (isStart) "во" else "о", isUpper)
                }
                'և' -> {
                    val isStart = i == 0 || !lowerText[i - 1].isLetter()
                    append(result, if (isStart) "йев" else "ев", isUpper)
                }
                else -> {
                    val transcription = map[char] ?: char.toString()
                    append(result, transcription, isUpper)
                }
            }
            i++
        }
        return result.toString()
    }

    private fun append(sb: StringBuilder, text: String, isUpper: Boolean) {
        if (isUpper && text.isNotEmpty()) {
            sb.append(text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        } else {
            sb.append(text)
        }
    }
}
