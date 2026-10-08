package com.jarvis.assistant.network

import com.jarvis.assistant.data.SecurePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to the Jarvis PC agent (see /pc-agent at the repo root) over plain HTTP - same LAN,
 * or a private network like Tailscale. Every call needs the pairing token from Settings;
 * there is no PC-side tool declaration here, that lives in ClaudeSchemas/GeminiSchemas -
 * this class only knows how to reach the agent, not when the model should call it.
 */
class PcAgentClient(private val securePrefs: SecurePrefs) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .build()

    suspend fun ping(): String = call("GET", "/ping", null) { json ->
        "Connecté à ${json.optString("hostname", "l'ordinateur")} (${json.optString("os", "?")})."
    }

    suspend fun runCommand(command: String): String =
        call("POST", "/run_command", JSONObject().put("command", command)) { json ->
            val stdout = json.optString("stdout")
            val stderr = json.optString("stderr")
            val exitCode = json.optInt("exit_code")
            buildString {
                append("Code de sortie : $exitCode")
                if (stdout.isNotBlank()) append("\nSortie :\n$stdout")
                if (stderr.isNotBlank()) append("\nErreurs :\n$stderr")
            }
        }

    suspend fun readFile(path: String): String =
        call("POST", "/read_file", JSONObject().put("path", path)) { it.optString("content") }

    suspend fun listFiles(path: String?): String =
        call("POST", "/list_files", JSONObject().apply { path?.let { put("path", it) } }) { json ->
            val entries = json.optJSONArray("entries") ?: return@call "(dossier vide)"
            (0 until entries.length()).joinToString("\n") { i ->
                val entry = entries.getJSONObject(i)
                val marker = if (entry.optString("type") == "dir") "[dossier]" else "[fichier]"
                "$marker ${entry.optString("name")}"
            }
        }

    suspend fun openPath(path: String): String =
        call("POST", "/open_path", JSONObject().put("path", path)) { it.optString("result") }

    private suspend fun call(
        method: String,
        route: String,
        body: JSONObject?,
        extract: (JSONObject) -> String
    ): String = withContext(Dispatchers.IO) {
        val baseUrl = securePrefs.pcAgentUrl
        val token = securePrefs.pcAgentToken
        if (baseUrl.isNullOrBlank() || token.isNullOrBlank()) {
            return@withContext "L'agent PC n'est pas configuré. Ouvre Paramètres > Contrôle du PC pour renseigner son adresse et son jeton."
        }

        val requestBuilder = Request.Builder()
            .url("$baseUrl$route")
            .header("Authorization", "Bearer $token")

        if (method == "POST") {
            requestBuilder.post((body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType()))
        }

        try {
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (response.code == 401) {
                    return@withContext "L'agent PC a refusé la connexion : jeton invalide. Vérifie-le dans Paramètres."
                }
                if (!response.isSuccessful) {
                    val error = runCatching { JSONObject(bodyString).optString("error") }.getOrNull()
                    return@withContext "L'agent PC a répondu une erreur : ${error ?: bodyString}"
                }
                extract(JSONObject(bodyString))
            }
        } catch (e: IOException) {
            "Impossible de contacter l'agent PC à $baseUrl : ${e.message}. Vérifie qu'il tourne et que le téléphone peut le joindre."
        }
    }
}
