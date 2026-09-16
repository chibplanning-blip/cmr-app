package com.jarvis.assistant.tools

import android.content.Context
import org.json.JSONObject

/** Tools whose effects are hard to undo or visible to other people - always confirmed with the user first. */
val SENSITIVE_TOOLS = setOf("call_contact", "send_sms", "toggle_bluetooth", "open_airplane_mode_settings")

/**
 * Dispatches a tool_use call from Claude to the matching Android action.
 * [confirm] is suspended until the user answers a confirmation dialog for sensitive tools;
 * it is not called at all for safe, read-only or easily-reversible tools.
 */
class ToolExecutor(context: Context) {

    private val appLauncher = AppLauncherTool(context)
    private val phoneActions = PhoneActionsTool(context)
    private val phoneSettings = PhoneSettingsTool(context)

    suspend fun execute(
        toolName: String,
        inputJson: String,
        confirm: suspend (message: String) -> Boolean
    ): String {
        val input = if (inputJson.isBlank()) JSONObject() else JSONObject(inputJson)

        if (toolName in SENSITIVE_TOOLS) {
            val confirmationMessage = describeForConfirmation(toolName, input)
            val approved = confirm(confirmationMessage)
            if (!approved) return "Action annulée par l'utilisateur."
        }

        return when (toolName) {
            "open_app" -> appLauncher.launch(input.getString("app_name"))
            "list_installed_apps" -> appLauncher.listApps()
            "call_contact" -> phoneActions.call(input.getString("name_or_number"))
            "send_sms" -> phoneActions.sendSms(input.getString("name_or_number"), input.getString("message"))
            "get_recent_notifications" -> NotificationRepository.formatForModel(input.optInt("count", 10))
            "set_volume" -> phoneSettings.setVolume(input.getInt("percent"))
            "set_brightness" -> phoneSettings.setBrightness(input.getInt("percent"))
            "toggle_wifi" -> phoneSettings.openWifiPanel()
            "toggle_bluetooth" -> phoneSettings.openBluetoothPanel()
            "open_airplane_mode_settings" -> phoneSettings.openAirplaneModeSettings()
            else -> "Outil inconnu : $toolName"
        }
    }

    private fun describeForConfirmation(toolName: String, input: JSONObject): String = when (toolName) {
        "call_contact" -> "Jarvis veut appeler « ${input.optString("name_or_number")} ». Confirmer ?"
        "send_sms" -> "Jarvis veut envoyer le SMS « ${input.optString("message")} » à « ${input.optString("name_or_number")} ». Confirmer ?"
        "toggle_bluetooth" -> "Jarvis veut activer le Bluetooth. Confirmer ?"
        "open_airplane_mode_settings" -> "Jarvis veut ouvrir les réglages du mode avion. Confirmer ?"
        else -> "Jarvis veut exécuter « $toolName ». Confirmer ?"
    }
}
