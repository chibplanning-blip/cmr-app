package com.jarvis.assistant.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.data.PermissionsHelper

data class OnboardingStep(val title: String, val explanation: String, val action: () -> Unit)

@Composable
fun OnboardingScreen(
    onRequestRuntimePermissions: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    val steps = listOf(
        OnboardingStep(
            title = "Micro, appels, SMS, contacts, Bluetooth",
            explanation = "Nécessaire pour te parler, appeler ou texter un contact et activer le Bluetooth.",
            action = onRequestRuntimePermissions
        ),
        OnboardingStep(
            title = "Accès aux notifications",
            explanation = "Pour que Jarvis puisse lire et résumer tes notifications récentes.",
            action = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        ),
        OnboardingStep(
            title = "Modifier les paramètres système",
            explanation = "Pour régler la luminosité de l'écran à ta demande.",
            action = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Bienvenue sur Jarvis", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text(
            "Jarvis a besoin de quelques autorisations pour pouvoir vraiment agir sur ton " +
                "téléphone. Tu peux toutes les revoir plus tard dans les réglages Android."
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(steps) { step ->
                Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(step.title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                        Text(step.explanation)
                        Button(onClick = step.action) { Text("Autoriser") }
                    }
                }
            }
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continuer")
        }
    }
}

fun notificationAccessGranted(context: android.content.Context): Boolean =
    PermissionsHelper.canReadNotifications(context)
