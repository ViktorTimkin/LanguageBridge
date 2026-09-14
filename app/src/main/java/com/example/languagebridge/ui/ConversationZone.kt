package com.example.languagebridge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.languagebridge.data.ConversationTurn
import com.example.languagebridge.data.Language

@Composable
fun ConversationZone(
    language: Language,
    conversation: List<ConversationTurn>,
    listState: LazyListState,
    hasMicPermission: Boolean,
    isBusy: Boolean,
    onPressStart: (Language) -> Unit,
    onPressEnd: (Language) -> Unit,
    flipped: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && hasMicPermission) {
            onPressStart(language)
        } else if (!isPressed) {
            onPressEnd(language)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .then(if (flipped) Modifier.rotate(180f) else Modifier)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            items(conversation) { turn ->
                Text(text = turn.textFor(language), modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Button(
            onClick = { /* реакция идёт через interactionSource ниже */ },
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                when {
                    !hasMicPermission -> "Нет разрешения на микрофон"
                    isPressed -> "Слушаю..."
                    isBusy -> "Обработка..."
                    else -> "Зажмите, чтобы сказать: ${language.displayName}"
                }
            )
        }
    }
}