package com.example.languagebridge.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.languagebridge.data.Language

@Composable
fun TypedInputRow(
    language: Language,
    onSend: (Language, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var typedText by remember { mutableStateOf("") }

    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        OutlinedTextField(
            value = typedText,
            onValueChange = { typedText = it },
            placeholder = { Text(language.displayName) },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                if (typedText.isNotBlank()) {
                    onSend(language, typedText)
                    typedText = ""
                }
            },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("➤")
        }
    }
}