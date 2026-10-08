package com.jarvis.assistant.network

import com.anthropic.core.JsonValue
import com.anthropic.models.messages.Tool

/** JSON-schema tool definitions Claude can call to control the phone. */
object ClaudeSchemas {

    private fun stringProp(description: String) = JsonValue.from(
        mapOf("type" to "string", "description" to description)
    )

    private fun intProp(description: String) = JsonValue.from(
        mapOf("type" to "integer", "description" to description)
    )

    private fun tool(
        name: String,
        description: String,
        properties: Map<String, JsonValue> = emptyMap(),
        required: List<String> = emptyList()
    ): Tool {
        val propsBuilder = Tool.InputSchema.Properties.builder()
        properties.forEach { (key, value) -> propsBuilder.putAdditionalProperty(key, value) }
        return Tool.builder()
            .name(name)
            .description(description)
            .inputSchema(
                Tool.InputSchema.builder()
                    .properties(propsBuilder.build())
                    .required(required)
                    .build()
            )
            .build()
    }

    val ALL: List<Tool> = listOf(
        tool(
            name = "open_app",
            description = "Ouvre une application installée sur le téléphone par son nom.",
            properties = mapOf("app_name" to stringProp("Nom de l'application, ex: Spotify")),
            required = listOf("app_name")
        ),
        tool(
            name = "list_installed_apps",
            description = "Liste les applications installées sur le téléphone."
        ),
        tool(
            name = "call_contact",
            description = "Passe un appel téléphonique vers un contact (par nom) ou un numéro. Action sensible : nécessite confirmation de l'utilisateur.",
            properties = mapOf("name_or_number" to stringProp("Nom du contact ou numéro de téléphone")),
            required = listOf("name_or_number")
        ),
        tool(
            name = "send_sms",
            description = "Envoie un SMS à un contact (par nom) ou un numéro. Action sensible : nécessite confirmation de l'utilisateur.",
            properties = mapOf(
                "name_or_number" to stringProp("Nom du contact ou numéro de téléphone"),
                "message" to stringProp("Contenu du SMS")
            ),
            required = listOf("name_or_number", "message")
        ),
        tool(
            name = "get_recent_notifications",
            description = "Récupère les notifications récentes reçues sur le téléphone (nécessite l'accès aux notifications).",
            properties = mapOf("count" to intProp("Nombre de notifications à récupérer, défaut 10"))
        ),
        tool(
            name = "set_volume",
            description = "Règle le volume média du téléphone.",
            properties = mapOf("percent" to intProp("Volume cible entre 0 et 100")),
            required = listOf("percent")
        ),
        tool(
            name = "set_brightness",
            description = "Règle la luminosité de l'écran.",
            properties = mapOf("percent" to intProp("Luminosité cible entre 1 et 100")),
            required = listOf("percent")
        ),
        tool(
            name = "toggle_wifi",
            description = "Ouvre le panneau rapide Wi-Fi (Android ne permet pas de le basculer automatiquement)."
        ),
        tool(
            name = "toggle_bluetooth",
            description = "Active le Bluetooth ou ouvre son panneau de réglage. Action sensible : nécessite confirmation."
        ),
        tool(
            name = "open_airplane_mode_settings",
            description = "Ouvre l'écran des réglages du mode avion. Action sensible : nécessite confirmation."
        ),
        tool(
            name = "set_alarm",
            description = "Règle une alarme à une heure donnée dans l'application horloge du téléphone.",
            properties = mapOf(
                "hour" to intProp("Heure entre 0 et 23"),
                "minute" to intProp("Minute entre 0 et 59"),
                "label" to stringProp("Libellé optionnel de l'alarme")
            ),
            required = listOf("hour", "minute")
        ),
        tool(
            name = "open_alarms_list",
            description = "Ouvre la liste des alarmes existantes."
        ),
        tool(
            name = "remember_fact",
            description = "Mémorise durablement une information sur l'utilisateur (prénom, préférence, habitude...), rappelée dans les conversations suivantes même après un redémarrage de l'app.",
            properties = mapOf("fact" to stringProp("L'information à retenir, formulée de façon concise")),
            required = listOf("fact")
        ),
        tool(
            name = "pc_run_command",
            description = "Exécute une commande shell sur l'ordinateur de l'utilisateur, via l'agent PC. Action sensible : nécessite confirmation de l'utilisateur.",
            properties = mapOf("command" to stringProp("Commande shell à exécuter")),
            required = listOf("command")
        ),
        tool(
            name = "pc_read_file",
            description = "Lit le contenu d'un fichier texte sur l'ordinateur de l'utilisateur, via l'agent PC.",
            properties = mapOf("path" to stringProp("Chemin absolu du fichier à lire")),
            required = listOf("path")
        ),
        tool(
            name = "pc_list_files",
            description = "Liste le contenu d'un dossier sur l'ordinateur de l'utilisateur, via l'agent PC.",
            properties = mapOf("path" to stringProp("Chemin absolu du dossier (dossier utilisateur par défaut si omis)"))
        ),
        tool(
            name = "pc_open_path",
            description = "Ouvre un fichier, une application ou un lien sur l'ordinateur de l'utilisateur avec le programme par défaut, via l'agent PC.",
            properties = mapOf("path" to stringProp("Chemin du fichier/app ou URL à ouvrir")),
            required = listOf("path")
        )
    )
}
