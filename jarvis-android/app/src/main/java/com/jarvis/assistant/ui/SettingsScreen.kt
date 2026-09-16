package com.jarvis.assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.data.SecurePrefs

@Composable
fun SettingsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePrefs(context) }

    var apiKey by remember { mutableStateOf(securePrefs.apiKey.orEmpty()) }
    var selectedModel by remember { mutableStateOf(securePrefs.model) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Paramètres", style = MaterialTheme.typography.headlineMedium)

        Text(
            "Clé API Anthropic (console.anthropic.com). Elle est stockée chiffrée sur ton " +
                "téléphone et n'est jamais envoyée ailleurs qu'à l'API Claude."
        )
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("sk-ant-...") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Modèle utilisé comme cerveau de Jarvis :", style = MaterialTheme.typography.titleMedium)
        SecurePrefs.AVAILABLE_MODELS.forEach { (modelId, label) ->
            Row {
                RadioButton(selected = selectedModel == modelId, onClick = { selectedModel = modelId })
                Text(label, modifier = Modifier.padding(top = 12.dp))
            }
        }

        Button(
            onClick = {
                securePrefs.apiKey = apiKey.trim()
                securePrefs.model = selectedModel
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer")
        }
    }
}
