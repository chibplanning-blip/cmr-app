package com.jarvis.assistant.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarvis.assistant.ui.theme.JarvisAmber
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisCyanDim
import com.jarvis.assistant.ui.theme.JarvisPanel
import com.jarvis.assistant.ui.theme.JarvisPanelLight
import com.jarvis.assistant.ui.theme.JarvisRed
import com.jarvis.assistant.ui.theme.JarvisText
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
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    pendingConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = { viewModel.answerConfirmation(false) },
            title = { Text("CONFIRMATION REQUISE", letterSpacing = 1.sp) },
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
                title = {
                    Text(
                        "JARVIS",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = JarvisCyan
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JarvisPanel),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Paramètres", tint = JarvisCyan)
                    }
                }
            )
        },
        floatingActionButton = { MicButton(state = state, onClick = { viewModel.startListening() }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).hudCorners()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (messages.isEmpty()) {
                    ReactorEmblem(modifier = Modifier.fillMaxSize())
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(role = message.role, text = message.text)
                    }
                }
            }

            StatusLabel(state)

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Écrire à Jarvis...") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCyanDim,
                        focusedTextColor = JarvisText,
                        unfocusedTextColor = JarvisText,
                        cursorColor = JarvisCyan,
                        focusedLabelColor = JarvisCyan,
                        unfocusedLabelColor = JarvisText
                    )
                )
                IconButton(onClick = {
                    viewModel.sendTypedText(textInput)
                    textInput = ""
                }) {
                    Text("➤", color = JarvisCyan, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(role: ChatRole, text: String) {
    val alignment = if (role == ChatRole.USER) Arrangement.End else Arrangement.Start
    val (containerColor, borderColor, textColor) = when (role) {
        ChatRole.USER -> Triple(JarvisCyanDim, JarvisCyan, JarvisText)
        ChatRole.ASSISTANT -> Triple(JarvisPanelLight, JarvisCyanDim, JarvisText)
        ChatRole.SYSTEM -> Triple(Color(0xFF4A1A1A), JarvisRed, JarvisText)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
        Card(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Text(text, color = textColor, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun StatusLabel(state: AssistantState) {
    val label = when (state) {
        AssistantState.IDLE -> null
        AssistantState.LISTENING -> "◉ JARVIS ÉCOUTE..."
        AssistantState.THINKING -> "◐ JARVIS RÉFLÉCHIT..."
        AssistantState.SPEAKING -> "▶ JARVIS RÉPOND..."
    }
    if (label != null) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                label,
                color = JarvisAmber,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Thin corner brackets, like a targeting HUD frame - the clearest "Iron Man" tell. */
private fun Modifier.hudCorners(): Modifier = this.drawBehind {
    val length = 22.dp.toPx()
    val inset = 6.dp.toPx()
    val stroke = 2.dp.toPx()
    val color = JarvisCyanDim

    // top-left
    drawLine(color, androidx.compose.ui.geometry.Offset(inset, inset), androidx.compose.ui.geometry.Offset(inset + length, inset), stroke)
    drawLine(color, androidx.compose.ui.geometry.Offset(inset, inset), androidx.compose.ui.geometry.Offset(inset, inset + length), stroke)
    // top-right
    drawLine(color, androidx.compose.ui.geometry.Offset(size.width - inset, inset), androidx.compose.ui.geometry.Offset(size.width - inset - length, inset), stroke)
    drawLine(color, androidx.compose.ui.geometry.Offset(size.width - inset, inset), androidx.compose.ui.geometry.Offset(size.width - inset, inset + length), stroke)
    // bottom-left
    drawLine(color, androidx.compose.ui.geometry.Offset(inset, size.height - inset), androidx.compose.ui.geometry.Offset(inset + length, size.height - inset), stroke)
    drawLine(color, androidx.compose.ui.geometry.Offset(inset, size.height - inset), androidx.compose.ui.geometry.Offset(inset, size.height - inset - length), stroke)
    // bottom-right
    drawLine(color, androidx.compose.ui.geometry.Offset(size.width - inset, size.height - inset), androidx.compose.ui.geometry.Offset(size.width - inset - length, size.height - inset), stroke)
    drawLine(color, androidx.compose.ui.geometry.Offset(size.width - inset, size.height - inset), androidx.compose.ui.geometry.Offset(size.width - inset, size.height - inset - length), stroke)
}

/** The arc-reactor emblem shown before the first message - concentric glowing rings. */
@Composable
private fun ReactorEmblem(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "reactor-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reactor-pulse-value"
    )

    Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val outerRadius = size.minDimension / 2
            drawCircle(color = JarvisCyan.copy(alpha = 0.10f * pulse), radius = outerRadius)
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.6f * pulse),
                radius = outerRadius * 0.75f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.9f),
                radius = outerRadius * 0.5f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(color = JarvisCyan.copy(alpha = 0.25f * pulse), radius = outerRadius * 0.35f)
        }
        Text(
            "JARVIS",
            color = JarvisCyan.copy(alpha = 0.85f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun MicButton(state: AssistantState, onClick: () -> Unit) {
    val active = state != AssistantState.IDLE
    val glowColor = when (state) {
        AssistantState.LISTENING -> JarvisRed
        AssistantState.THINKING -> JarvisAmber
        else -> JarvisCyan
    }

    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.size(84.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        if (active) {
            Canvas(modifier = Modifier.size(84.dp)) {
                drawCircle(color = glowColor.copy(alpha = pulse * 0.35f), radius = size.minDimension / 2)
            }
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (active) glowColor else JarvisPanelLight,
            contentColor = if (active) JarvisPanel else JarvisCyan,
            modifier = Modifier
                .size(64.dp)
                .drawBehind {
                    if (active) {
                        drawCircle(color = glowColor.copy(alpha = 0.5f), radius = size.minDimension / 2 + 6f)
                    }
                }
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Parler")
        }
    }
}
