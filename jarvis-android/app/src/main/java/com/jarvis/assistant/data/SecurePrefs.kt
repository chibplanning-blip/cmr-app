package com.jarvis.assistant.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted storage for the Google AI (Gemini) API key and the chosen model.
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

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL = "selected_model"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // Gemini 2.0 Flash is squarely in Google AI Studio's free tier (no card required).
        // "gemini-flash-latest" always points at the newest Flash model if you want to
        // follow updates automatically instead of pinning a version.
        const val DEFAULT_MODEL = "gemini-2.0-flash"

        val AVAILABLE_MODELS = listOf(
            "gemini-2.0-flash" to "Gemini 2.0 Flash — gratuit, rapide (par défaut)",
            "gemini-flash-latest" to "Gemini Flash (dernière version) — suit les mises à jour de Google",
            "gemini-2.5-flash" to "Gemini 2.5 Flash — plus capable, toujours gratuit dans la limite du forfait"
        )
    }
}
