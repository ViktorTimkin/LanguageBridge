package com.example.languagebridge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.languagebridge.data.AzureTranslationService
import com.example.languagebridge.data.Language
import com.example.languagebridge.data.TranslatorViewModel
import com.example.languagebridge.ui.theme.LanguageBridgeTheme

class MainActivity : ComponentActivity() {

    private var micPermissionGranted by mutableStateOf(false)

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermissionGranted = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        micPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val service = AzureTranslationService(
            speechKey = BuildConfig.AZURE_SPEECH_KEY,
            speechRegion = BuildConfig.AZURE_SPEECH_REGION
        )
        val viewModel = TranslatorViewModel(service)

        setContent {
            LanguageBridgeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        viewModel = viewModel,
                        hasMicPermission = micPermissionGranted,
                        onRequestMicPermission = {
                            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun Greeting(
    viewModel: TranslatorViewModel,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var topLanguage by remember { mutableStateOf(Language.ARMENIAN) }
    val bottomLanguage = topLanguage.other()

    val topListState = rememberLazyListState()
    val bottomListState = rememberLazyListState()

    LaunchedEffect(viewModel.conversation.size) {
        val lastIndex = viewModel.conversation.size - 1
        if (lastIndex >= 0) {
            topListState.animateScrollToItem(lastIndex)
            bottomListState.animateScrollToItem(lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        // Верхняя зона — язык задаётся переменной topLanguage, а не жёстко
        ConversationZone(
            language = topLanguage,
            conversation = viewModel.conversation,
            listState = topListState,
            hasMicPermission = hasMicPermission,
            isBusy = viewModel.isBusy,
            onRequestMicPermission = onRequestMicPermission,
            onStart = { source, target -> viewModel.startTranslation(source, target) },
            flipped = true, // верхняя зона всегда перевёрнута, независимо от языка
            modifier = Modifier.weight(1f)
        )

        // Средняя полоска с кнопкой смены сторон
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(vertical = 8.dp)
        ) {
            Button(onClick = {
                topLanguage = topLanguage.other()
            }) {
                Text("⇅ Поменять стороны")
            }

            viewModel.errorMessage?.let {
                Text(
                    text = "Ошибка: $it",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Нижняя зона — всегда "другой" язык, обычная ориентация
        ConversationZone(
            language = bottomLanguage,
            conversation = viewModel.conversation,
            listState = bottomListState,
            hasMicPermission = hasMicPermission,
            isBusy = viewModel.isBusy,
            onRequestMicPermission = onRequestMicPermission,
            onStart = { source, target -> viewModel.startTranslation(source, target) },
            flipped = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ConversationZone(
    language: Language,
    conversation: List<com.example.languagebridge.data.ConversationTurn>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    hasMicPermission: Boolean,
    isBusy: Boolean,
    onRequestMicPermission: () -> Unit,
    onStart: (source: Language, target: Language) -> Unit,
    flipped: Boolean,
    modifier: Modifier = Modifier
) {
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
            onClick = {
                if (hasMicPermission) {
                    onStart(language, language.other())
                } else {
                    onRequestMicPermission()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                when {
                    !hasMicPermission -> "Разрешить микрофон"
                    isBusy -> "Слушаю..."
                    else -> "Говорить: ${language.displayName}"
                }
            )
        }
    }
}

@Composable
private fun SpeakButton(
    language: Language,
    hasMicPermission: Boolean,
    isBusy: Boolean,
    onRequestMicPermission: () -> Unit,
    onStart: (source: Language, target: Language) -> Unit
) {
    Button(
        onClick = {
            if (hasMicPermission) {
                onStart(language, language.other())
            } else {
                onRequestMicPermission()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            when {
                !hasMicPermission -> "Разрешить микрофон"
                isBusy -> "Слушаю..."
                else -> "Говорить: ${language.displayName}"
            }
        )
    }
}