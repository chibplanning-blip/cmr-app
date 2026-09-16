package com.jarvis.assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarvis.assistant.viewmodel.AssistantState
import com.jarvis.assistant.viewmodel.ChatRole
import com.jarvis.assistant.viewmodel.ChatViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onOpenSettings: () -> Unit, viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val state by viewModel.state.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    var textInput by remember { mutableStateOf("") }

    pendingConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = { viewModel.answerConfirmation(false) },
            title = { Text("Confirmation requise") },
            text = { Text(confirmation.message) },
            confirmButton = {
                TextButton(onClick = { viewModel.answerConfirmation(true) }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.answerConfirmation(false) }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jarvis") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.startListening() },
                containerColor = if (state == AssistantState.LISTENING) Color.Red else MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Parler")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val alignment = if (message.role == ChatRole.USER) Arrangement.End else Arrangement.Start
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (message.role) {
                                    ChatRole.USER -> MaterialTheme.colorScheme.primaryContainer
                                    ChatRole.ASSISTANT -> MaterialTheme.colorScheme.secondaryContainer
                                    ChatRole.SYSTEM -> MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        ) {
                            Text(message.text, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }

            StatusLabel(state)

            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Écrire à Jarvis...") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    viewModel.sendTypedText(textInput)
                    textInput = ""
                }) {
                    Text("Envoyer")
                }
            }
        }
    }
}

@Composable
private fun StatusLabel(state: AssistantState) {
    val label = when (state) {
        AssistantState.IDLE -> null
        AssistantState.LISTENING -> "Jarvis écoute..."
        AssistantState.THINKING -> "Jarvis réfléchit..."
        AssistantState.SPEAKING -> "Jarvis répond..."
    }
    if (label != null) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
