package com.jarvis.assistant.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray

enum class AiProvider { GEMINI, CLAUDE }

/**
 * Encrypted storage for both providers' API keys and chosen models, plus which provider
 * is currently active. Both keys are kept even when only one is active, so switching
 * providers in Settings never requires re-entering a key that was already saved.
 * Never store a key in plain SharedPreferences or log it.
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

    var provider: AiProvider
        get() = if (prefs.getString(KEY_PROVIDER, null) == AiProvider.CLAUDE.name) {
            AiProvider.CLAUDE
        } else {
            AiProvider.GEMINI
        }
        set(value) = prefs.edit().putString(KEY_PROVIDER, value.name).apply()

    var geminiApiKey: String?
        get() = prefs.getString(KEY_GEMINI_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_GEMINI_API_KEY, value).apply()

    var claudeApiKey: String?
        get() = prefs.getString(KEY_CLAUDE_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_CLAUDE_API_KEY, value).apply()

    var geminiModel: String
        get() = prefs.getString(KEY_GEMINI_MODEL, DEFAULT_GEMINI_MODEL) ?: DEFAULT_GEMINI_MODEL
        set(value) = prefs.edit().putString(KEY_GEMINI_MODEL, value).apply()

    var claudeModel: String
        get() = prefs.getString(KEY_CLAUDE_MODEL, DEFAULT_CLAUDE_MODEL) ?: DEFAULT_CLAUDE_MODEL
        set(value) = prefs.edit().putString(KEY_CLAUDE_MODEL, value).apply()

    /** The key and model for whichever provider is currently selected. */
    val activeApiKey: String? get() = if (provider == AiProvider.CLAUDE) claudeApiKey else geminiApiKey
    val activeModel: String get() = if (provider == AiProvider.CLAUDE) claudeModel else geminiModel
    val hasApiKey: Boolean get() = !activeApiKey.isNullOrBlank()

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    /** Facts Jarvis has learned about the user, kept across app restarts and conversations. */
    fun memoryFacts(): List<String> {
        val raw = prefs.getString(KEY_MEMORY_FACTS, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { array.getString(it) }
    }

    fun rememberFact(fact: String) {
        val facts = (memoryFacts() + fact).takeLast(MAX_MEMORY_FACTS)
        val array = JSONArray()
        facts.forEach { array.put(it) }
        prefs.edit().putString(KEY_MEMORY_FACTS, array.toString()).apply()
    }

    fun forgetEverything() {
        prefs.edit().remove(KEY_MEMORY_FACTS).remove(KEY_CHAT_HISTORY).apply()
    }

    /** Chat transcript persisted as JSON [{"role": "...", "text": "..."}] so it survives an app restart. */
    var chatHistoryJson: String?
        get() = prefs.getString(KEY_CHAT_HISTORY, null)
        set(value) = prefs.edit().putString(KEY_CHAT_HISTORY, value).apply()

    companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_CLAUDE_API_KEY = "claude_api_key"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_CLAUDE_MODEL = "claude_model"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_MEMORY_FACTS = "memory_facts"
        private const val KEY_CHAT_HISTORY = "chat_history"
        private const val MAX_MEMORY_FACTS = 50

        // Gemini 2.0 Flash is squarely in Google AI Studio's free tier (no card required).
        const val DEFAULT_GEMINI_MODEL = "gemini-2.0-flash"
        const val DEFAULT_CLAUDE_MODEL = "claude-opus-5"

        val GEMINI_MODELS = listOf(
            "gemini-2.0-flash" to "Gemini 2.0 Flash — gratuit, rapide (par défaut)",
            "gemini-flash-latest" to "Gemini Flash (dernière version) — suit les mises à jour de Google",
            "gemini-2.5-flash" to "Gemini 2.5 Flash — plus capable, toujours gratuit dans la limite du forfait"
        )

        val CLAUDE_MODELS = listOf(
            "claude-opus-5" to "Claude Opus 5 — le plus capable (par défaut)",
            "claude-sonnet-5" to "Claude Sonnet 5 — bon compromis coût/qualité",
            "claude-haiku-4-5" to "Claude Haiku 4.5 — le moins cher, plus rapide"
        )
    }
}
