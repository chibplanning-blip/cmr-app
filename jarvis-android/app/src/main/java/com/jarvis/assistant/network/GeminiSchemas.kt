package com.jarvis.assistant.network

import org.json.JSONArray
import org.json.JSONObject

/**
 * Function declarations Gemini can call to control the phone, in Gemini's own schema
 * shape (uppercase JSON-Schema-ish types: OBJECT/STRING/INTEGER, not lowercase "object").
 */
object GeminiSchemas {

    private fun stringProp(description: String) = JSONObject()
        .put("type", "STRING")
        .put("description", description)

    private fun intProp(description: String) = JSONObject()
        .put("type", "INTEGER")
        .put("description", description)

    private fun declaration(
        name: String,
        description: String,
        properties: Map<String, JSONObject> = emptyMap(),
        required: List<String> = emptyList()
    ): JSONObject {
        val params = JSONObject().put("type", "OBJECT")
        if (properties.isNotEmpty()) {
            val propsObj = JSONObject()
            properties.forEach { (key, value) -> propsObj.put(key, value) }
            params.put("properties", propsObj)
        }
        if (required.isNotEmpty()) {
            params.put("required", JSONArray(required))
        }
        return JSONObject()
            .put("name", name)
            .put("description", description)
            .put("parameters", params)
    }

    val ALL: JSONArray = JSONArray().apply {
        put(
            declaration(
                name = "open_app",
                description = "Ouvre une application installée sur le téléphone par son nom.",
                properties = mapOf("app_name" to stringProp("Nom de l'application, ex: Spotify")),
                required = listOf("app_name")
            )
        )
        put(
            declaration(
                name = "list_installed_apps",
                description = "Liste les applications installées sur le téléphone."
            )
        )
        put(
            declaration(
                name = "call_contact",
                description = "Passe un appel téléphonique vers un contact (par nom) ou un numéro. Action sensible : nécessite confirmation de l'utilisateur.",
                properties = mapOf("name_or_number" to stringProp("Nom du contact ou numéro de téléphone")),
                required = listOf("name_or_number")
            )
        )
        put(
            declaration(
                name = "send_sms",
                description = "Envoie un SMS à un contact (par nom) ou un numéro. Action sensible : nécessite confirmation de l'utilisateur.",
                properties = mapOf(
                    "name_or_number" to stringProp("Nom du contact ou numéro de téléphone"),
                    "message" to stringProp("Contenu du SMS")
                ),
                required = listOf("name_or_number", "message")
            )
        )
        put(
            declaration(
                name = "get_recent_notifications",
                description = "Récupère les notifications récentes reçues sur le téléphone (nécessite l'accès aux notifications).",
                properties = mapOf("count" to intProp("Nombre de notifications à récupérer, défaut 10"))
            )
        )
        put(
            declaration(
                name = "set_volume",
                description = "Règle le volume média du téléphone.",
                properties = mapOf("percent" to intProp("Volume cible entre 0 et 100")),
                required = listOf("percent")
            )
        )
        put(
            declaration(
                name = "set_brightness",
                description = "Règle la luminosité de l'écran.",
                properties = mapOf("percent" to intProp("Luminosité cible entre 1 et 100")),
                required = listOf("percent")
            )
        )
        put(
            declaration(
                name = "toggle_wifi",
                description = "Ouvre le panneau rapide Wi-Fi (Android ne permet pas de le basculer automatiquement)."
            )
        )
        put(
            declaration(
                name = "toggle_bluetooth",
                description = "Active le Bluetooth ou ouvre son panneau de réglage. Action sensible : nécessite confirmation."
            )
        )
        put(
            declaration(
                name = "open_airplane_mode_settings",
                description = "Ouvre l'écran des réglages du mode avion. Action sensible : nécessite confirmation."
            )
        )
        put(
            declaration(
                name = "set_alarm",
                description = "Règle une alarme à une heure donnée dans l'application horloge du téléphone.",
                properties = mapOf(
                    "hour" to intProp("Heure entre 0 et 23"),
                    "minute" to intProp("Minute entre 0 et 59"),
                    "label" to stringProp("Libellé optionnel de l'alarme")
                ),
                required = listOf("hour", "minute")
            )
        )
        put(
            declaration(
                name = "open_alarms_list",
                description = "Ouvre la liste des alarmes existantes."
            )
        )
        put(
            declaration(
                name = "remember_fact",
                description = "Mémorise durablement une information sur l'utilisateur (prénom, préférence, habitude...), rappelée dans les conversations suivantes même après un redémarrage de l'app.",
                properties = mapOf("fact" to stringProp("L'information à retenir, formulée de façon concise")),
                required = listOf("fact")
            )
        )
    }
}
