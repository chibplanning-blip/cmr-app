package com.jarvis.assistant.network

import com.jarvis.assistant.tools.ToolExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val MAX_TOOL_ITERATIONS = 8
private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

/**
 * Talks to the Gemini REST API. Kept as raw JSON (rather than a typed model) since there
 * is no official, actively maintained Kotlin SDK for the Gemini API.
 */
class GeminiClient(private val apiKey: String, private val model: String) : AiClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    private val history = mutableListOf<JSONObject>()

    override suspend fun sendMessage(
        userText: String,
        toolExecutor: ToolExecutor,
        confirm: suspend (String) -> Boolean
    ): String = withContext(Dispatchers.IO) {
        history.add(
            JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", userText)))
        )

        repeat(MAX_TOOL_ITERATIONS) {
            val modelTurn = callGemini(history)
            history.add(modelTurn)

            val parts = modelTurn.getJSONArray("parts")
            val functionCalls = mutableListOf<JSONObject>()
            val textParts = mutableListOf<String>()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                when {
                    part.has("functionCall") -> functionCalls.add(part.getJSONObject("functionCall"))
                    part.has("text") -> textParts.add(part.getString("text"))
                }
            }

            if (functionCalls.isEmpty()) {
                return@withContext textParts.joinToString("\n")
            }

            val responseParts = JSONArray()
            for (call in functionCalls) {
                val name = call.getString("name")
                val args = call.optJSONObject("args") ?: JSONObject()
                val resultText = try {
                    toolExecutor.execute(name, args.toString(), confirm)
                } catch (e: Exception) {
                    "Erreur lors de l'exécution de l'outil : ${e.message}"
                }
                responseParts.put(
                    JSONObject().put(
                        "functionResponse",
                        JSONObject()
                            .put("name", name)
                            .put("response", JSONObject().put("result", resultText))
                    )
                )
            }

            history.add(JSONObject().put("role", "user").put("parts", responseParts))
        }
        "Désolé, j'ai eu besoin de trop d'étapes pour répondre à cette demande."
    }

    private fun callGemini(history: List<JSONObject>): JSONObject {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray(history))
            put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", JARVIS_SYSTEM_PROMPT))))
            put("tools", JSONArray().put(JSONObject().put("functionDeclarations", GeminiSchemas.ALL)))
        }

        val request = Request.Builder()
            .url("$BASE_URL/$model:generateContent?key=$apiKey")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw java.io.IOException("Gemini a répondu ${response.code} : $bodyString")
            }
            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
                ?: throw java.io.IOException("Réponse Gemini sans candidat : $bodyString")
            return candidates.getJSONObject(0).getJSONObject("content")
        }
    }
}
