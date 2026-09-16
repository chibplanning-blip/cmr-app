package com.jarvis.assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.data.AiProvider
import com.jarvis.assistant.data.SecurePrefs
import com.jarvis.assistant.data.UpdateChecker
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePrefs(context) }
    val updateChecker = remember { UpdateChecker(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedProvider by remember { mutableStateOf(securePrefs.provider) }
    var geminiKey by remember { mutableStateOf(securePrefs.geminiApiKey.orEmpty()) }
    var claudeKey by remember { mutableStateOf(securePrefs.claudeApiKey.orEmpty()) }
    var geminiModel by remember { mutableStateOf(securePrefs.geminiModel) }
    var claudeModel by remember { mutableStateOf(securePrefs.claudeModel) }
    var updateStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Paramètres", style = MaterialTheme.typography.headlineMedium)

        Text("Cerveau de Jarvis :", style = MaterialTheme.typography.titleMedium)
        Row {
            RadioButton(
                selected = selectedProvider == AiProvider.GEMINI,
                onClick = { selectedProvider = AiProvider.GEMINI }
            )
            Text("Google Gemini (gratuit)", modifier = Modifier.padding(top = 12.dp))
        }
        Row {
            RadioButton(
                selected = selectedProvider == AiProvider.CLAUDE,
                onClick = { selectedProvider = AiProvider.CLAUDE }
            )
            Text("Anthropic Claude (payant, ta clé)", modifier = Modifier.padding(top = 12.dp))
        }

        when (selectedProvider) {
            AiProvider.GEMINI -> {
                Text(
                    "Clé API Google AI, gratuite (aistudio.google.com/apikey, aucune carte " +
                        "requise). Stockée chiffrée sur ton téléphone."
                )
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("AIza...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Modèle Gemini :", style = MaterialTheme.typography.titleMedium)
                SecurePrefs.GEMINI_MODELS.forEach { (modelId, label) ->
                    Row {
                        RadioButton(selected = geminiModel == modelId, onClick = { geminiModel = modelId })
                        Text(label, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }

            AiProvider.CLAUDE -> {
                Text(
                    "Clé API Anthropic, payante à l'usage (console.anthropic.com, carte + " +
                        "crédit prépayé requis). Stockée chiffrée sur ton téléphone."
                )
                OutlinedTextField(
                    value = claudeKey,
                    onValueChange = { claudeKey = it },
                    label = { Text("sk-ant-...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Modèle Claude :", style = MaterialTheme.typography.titleMedium)
                SecurePrefs.CLAUDE_MODELS.forEach { (modelId, label) ->
                    Row {
                        RadioButton(selected = claudeModel == modelId, onClick = { claudeModel = modelId })
                        Text(label, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }

        Button(
            onClick = {
                securePrefs.provider = selectedProvider
                securePrefs.geminiApiKey = geminiKey.trim()
                securePrefs.claudeApiKey = claudeKey.trim()
                securePrefs.geminiModel = geminiModel
                securePrefs.claudeModel = claudeModel
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer")
        }

        Text("Mises à jour :", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = {
                updateStatus = "Vérification..."
                coroutineScope.launch {
                    val update = updateChecker.checkForUpdate()
                    if (update == null) {
                        updateStatus = "Jarvis est déjà à jour."
                    } else {
                        updateStatus = "Nouvelle version trouvée, téléchargement..."
                        try {
                            updateChecker.downloadAndInstall(update)
                            updateStatus = "Installation lancée."
                        } catch (e: Exception) {
                            updateStatus = "Échec de la mise à jour : ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vérifier les mises à jour")
        }
        updateStatus?.let { Text(it) }
    }
}
