package com.example.languagebridge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
    var sourceLanguage by remember { mutableStateOf(Language.RUSSIAN) }
    val targetLanguage = sourceLanguage.other()

    // Каждый список хранит позицию прокрутки своей LazyColumn отдельно
    val armenianListState = rememberLazyListState()
    val russianListState = rememberLazyListState()

    // Срабатывает заново каждый раз, когда меняется conversation.size —
    // то есть когда добавляется новая реплика. Прокручиваем оба списка к последнему элементу.
    LaunchedEffect(viewModel.conversation.size) {
        val lastIndex = viewModel.conversation.size - 1
        if (lastIndex >= 0) {
            armenianListState.animateScrollToItem(lastIndex)
            russianListState.animateScrollToItem(lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        // Верхняя панель — армянский
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E)) // тёмно-серый фон, чтобы отличался от чёрного экрана
        ) {
            if (viewModel.conversation.isEmpty()) {
                Text(
                    text = "Здесь появится текст на армянском",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .rotate(180f)
                )
            } else {
                LazyColumn(
                    state = armenianListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(180f)
                        .padding(12.dp)
                ) {
                    items(viewModel.conversation) { turn ->
                        Text(text = turn.armenianText, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

// Центральная панель управления — фиксированной высоты, без weight
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Language Bridge")

            Row {
                Button(
                    onClick = { /* выбор языка вручную пока не реализован */ }
                ) {
                    Text(sourceLanguage.displayName)
                }

                Button(onClick = {
                    sourceLanguage = sourceLanguage.other()
                }) {
                    Text("⇅")
                }

                Button(
                    onClick = { /* выбор языка вручную пока не реализован */ }
                ) {
                    Text(targetLanguage.displayName)
                }
            }

            Button(onClick = {
                if (hasMicPermission) {
                    viewModel.startTranslation(sourceLanguage, targetLanguage)
                } else {
                    onRequestMicPermission()
                }
            }) {
                Text(
                    when {
                        !hasMicPermission -> "Разрешить микрофон"
                        viewModel.isBusy -> "Слушаю..."
                        else -> "Продолжить"
                    }
                )
            }

            viewModel.errorMessage?.let {
                Text("Ошибка: $it")
            }
        }

        // Нижняя панель — русский, обычная ориентация
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
        ) {
            if (viewModel.conversation.isEmpty()) {
                Text(
                    text = "Здесь появится текст на русском",
                    modifier = Modifier
                        .align(Alignment.Center)

                )
            } else {
                LazyColumn(
                    state = russianListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    items(viewModel.conversation) { turn ->
                        Text(text = turn.russianText, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}