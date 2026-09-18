package com.example.languagebridge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.example.languagebridge.data.ConversationTurn
import com.example.languagebridge.data.Language
import com.example.languagebridge.ui.theme.AppColors

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
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var isRecordingSession by remember { mutableStateOf(value = false) }

    LaunchedEffect(isPressed) {
        if (isPressed && hasMicPermission) {
            isRecordingSession = true
            onPressStart(language)
        } else if (!isPressed && isRecordingSession) {
            isRecordingSession = false
            onPressEnd(language)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.ZoneBackground)
            .then(if (flipped) Modifier.rotate(180f) else Modifier),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            items(conversation) { turn ->
                Text(
                    text = turn.textFor(language),
                    color = AppColors.TextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.BubbleBackground)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        Button(
            onClick = { /* реакция идёт через interactionSource ниже */ },
            interactionSource = interactionSource,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Text(
                text = when {
                    !hasMicPermission -> "Нет разрешения на микрофон"
                    isPressed -> "Слушаю..."
                    isBusy -> "Обработка..."
                    else -> "Зажмите, чтобы сказать: ${language.displayName}"
                },
                color = AppColors.TextPrimary,
            )
        }
    }
}