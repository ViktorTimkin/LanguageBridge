package com.example.languagebridge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.languagebridge.data.AzureTranslationService
import com.example.languagebridge.data.ConversationTurn
import com.example.languagebridge.data.Language
import com.example.languagebridge.ui.TranslatorViewModel
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

        if (!micPermissionGranted) {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }

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

        ConversationZone(
            language = topLanguage,
            conversation = viewModel.conversation,
            listState = topListState,
            hasMicPermission = hasMicPermission,
            isBusy = viewModel.isBusy,
            onPressStart = { lang -> viewModel.startListening(lang) },
            onPressEnd = { lang -> viewModel.stopListening(lang) },
            flipped = true,
            modifier = Modifier.weight(1f)
        )

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

        ConversationZone(
            language = bottomLanguage,
            conversation = viewModel.conversation,
            listState = bottomListState,
            hasMicPermission = hasMicPermission,
            isBusy = viewModel.isBusy,
            onPressStart = { lang -> viewModel.startListening(lang) },
            onPressEnd = { lang -> viewModel.stopListening(lang) },
            flipped = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ConversationZone(
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