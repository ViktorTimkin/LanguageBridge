package com.example.languagebridge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.core.content.ContextCompat
import com.example.languagebridge.data.AzureTranslationService
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
    var sourceLanguage by remember { mutableStateOf("Русский") }

    val translationLanguage =
        if (sourceLanguage == "Русский") "Հայերեն" else "Русский"

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Language Bridge")

        Text(text = "Язык оригинала")

        Button(
            onClick = {}
        ) {
            Text(sourceLanguage)
        }

        Button(
            onClick = {
                sourceLanguage =
                    if (sourceLanguage == "Русский") "Հայերեն" else "Русский"
            }
        ) {
            Text("⇅")
        }

        Text(text = "Язык перевода")

        Button(
            onClick = {}
        ) {
            Text(translationLanguage)
        }

        Button(
            onClick = {
                if (hasMicPermission) {
                    viewModel.startTranslation(sourceLanguage, translationLanguage)
                } else {
                    onRequestMicPermission()
                }
            }
        ) {
            Text(
                when {
                    !hasMicPermission -> "Разрешить микрофон"
                    viewModel.isBusy -> "Слушаю..."
                    else -> "Продолжить"
                }
            )
        }

        if (viewModel.recognizedText.isNotEmpty()) {
            Text("Вы сказали: ${viewModel.recognizedText}")
        }
        if (viewModel.translatedText.isNotEmpty()) {
            Text("Перевод: ${viewModel.translatedText}")
        }
        viewModel.errorMessage?.let {
            Text("Ошибка: $it")
        }
    }
}