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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import com.example.languagebridge.data.ConversationTurn
import com.example.languagebridge.data.Language
import com.example.languagebridge.ui.theme.AppColors
import androidx.compose.ui.graphics.Color
@Composable
fun ConversationZone(
    language: Language,
    conversation: List<ConversationTurn>,
    listState: LazyListState,
    hasMicPermission: Boolean,
    isBusy: Boolean,
    onPressStart: (Language) -> Unit,
    onPressEnd: (Language) -> Unit,
    onSpeak: (ConversationTurn) -> Unit,
    flipped: Boolean,
    contentFlippedExtra: Boolean = false,
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
                .padding(12.dp)
                .then(if (contentFlippedExtra) Modifier.rotate(180f) else Modifier),
        ) {
            items(conversation) { turn ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = turn.textFor(language),
                        color = AppColors.TextPrimary,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.BubbleBackground)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    IconButton(onClick = { onSpeak(turn) }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Replay audio",
                            tint = AppColors.AccentBlue
                        )
                    }
                }
            }
        }

        Button(
            onClick = {},
            interactionSource = interactionSource,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = AppColors.TextPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(AppColors.flagBrush(language))
                .background(Color.Black.copy(alpha = 0.35f)),
        ) {
            Text(
                text = when {
                    !hasMicPermission -> "Нет разрешения на микрофон"
                    isPressed -> "Слушаю..."
                    isBusy -> "Обработка..."
                    else -> "Зажмите, чтобы сказать: ${language.displayName}"
                },
                color = AppColors.TextPrimary,
                modifier = Modifier.then(if (contentFlippedExtra) Modifier.rotate(180f) else Modifier),
            )
        }
    }
}