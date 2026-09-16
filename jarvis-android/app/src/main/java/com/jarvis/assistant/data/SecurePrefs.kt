package com.jarvis.assistant.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted storage for the Anthropic API key and the chosen model.
 * Never store the key in plain SharedPreferences or log it.
 */
class SecurePrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "jarvis_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var apiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    val hasApiKey: Boolean get() = !apiKey.isNullOrBlank()

    companion object {
        private const val KEY_API_KEY = "anthropic_api_key"
        private const val KEY_MODEL = "selected_model"

        // Anthropic's most capable current model. Configurable in Settings
        // since a voice assistant used many times a day racks up real cost -
        // Sonnet 5 / Haiku 4.5 are cheaper alternatives.
        const val DEFAULT_MODEL = "claude-opus-5"

        val AVAILABLE_MODELS = listOf(
            "claude-opus-5" to "Claude Opus 5 — le plus capable (par défaut)",
            "claude-sonnet-5" to "Claude Sonnet 5 — bon compromis coût/qualité",
            "claude-haiku-4-5" to "Claude Haiku 4.5 — le moins cher, plus rapide"
        )
    }
}
