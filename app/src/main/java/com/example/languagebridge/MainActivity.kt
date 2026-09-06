package com.example.languagebridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.languagebridge.ui.theme.LanguageBridgeTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.languagebridge.data.TranslatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageBridgeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
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
    onRequestMicPermission: () -> Unit,
    modifier: Modifier = Modifier,
    hasMicPermission: Boolean, //разрешения на микрофон

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

        Text(
            text = "Язык перевода"
        )

        Button(
            onClick = {}
        ) {
            Text(translationLanguage)
        }

        Button(
            onClick = {}
        ) {
            Text("Продолжить")
        }
        Button(
            onClick = {}
        ) {

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LanguageBridgeTheme {
        Greeting("Android")
    }
}