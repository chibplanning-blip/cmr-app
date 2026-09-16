package com.jarvis.assistant.network

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.ContentBlockParam
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.anthropic.models.messages.StopReason
import com.anthropic.models.messages.ToolResultBlockParam
import com.jarvis.assistant.tools.ToolExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_TOOL_ITERATIONS = 8
private const val MAX_TOKENS = 4096L

private const val SYSTEM_PROMPT = """
Tu es Jarvis, l'assistant vocal personnel installé sur le téléphone Android de l'utilisateur.
Réponds toujours en français, de façon concise et naturelle à l'oral (pas de listes à puces,
pas de markdown - tes réponses sont lues à voix haute par un synthétiseur vocal).
Utilise les outils à ta disposition pour agir réellement sur le téléphone quand c'est pertinent
(ouvrir une app, lire les notifications, régler le volume ou la luminosité, appeler ou envoyer
un SMS). Pour les actions sensibles (appel, SMS, Bluetooth, mode avion), l'utilisateur devra
confirmer avant que l'action ne s'exécute réellement - explique brièvement ce que tu es en train
de faire.
"""

/**
 * Thin wrapper around the Anthropic Java SDK running the manual tool-use agent loop:
 * call the model, execute any requested tools (with confirmation for sensitive ones),
 * feed results back, repeat until the model produces a final text answer.
 */
class ClaudeClient(apiKey: String, private val model: String) {

    private val client = AnthropicOkHttpClient.builder().apiKey(apiKey).build()

    /**
     * [history] is mutated in place so the caller keeps the running conversation.
     * [confirm] is awaited only for sensitive tool calls (see ToolExecutor.SENSITIVE_TOOLS).
     * Returns the final assistant text to speak/display.
     */
    suspend fun sendAndResolve(
        history: MutableList<MessageParam>,
        toolExecutor: ToolExecutor,
        confirm: suspend (String) -> Boolean
    ): String = withContext(Dispatchers.IO) {
        repeat(MAX_TOOL_ITERATIONS) {
            val builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(MAX_TOKENS)
                .system(SYSTEM_PROMPT)
                .messages(history)
            ToolSchemas.ALL.forEach { builder.addTool(it) }

            val response = client.messages().create(builder.build())

            val assistantParam = MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .contentOfBlockParams(response.content().map { it.toParam() })
                .build()
            history.add(assistantParam)

            if (response.stopReason().orElse(null) != StopReason.TOOL_USE) {
                return@withContext response.content()
                    .mapNotNull { block -> block.text().orElse(null)?.text() }
                    .joinToString("\n")
            }

            val toolResults = mutableListOf<ContentBlockParam>()
            for (block in response.content()) {
                val toolUse = block.toolUse().orElse(null) ?: continue
                val resultText = try {
                    toolExecutor.execute(toolUse.name(), toolUse._input().toString(), confirm)
                } catch (e: Exception) {
                    "Erreur lors de l'exécution de l'outil : ${e.message}"
                }
                toolResults.add(
                    ContentBlockParam.ofToolResult(
                        ToolResultBlockParam.builder()
                            .toolUseId(toolUse.id())
                            .content(resultText)
                            .build()
                    )
                )
            }

            history.add(
                MessageParam.builder()
                    .role(MessageParam.Role.USER)
                    .contentOfBlockParams(toolResults)
                    .build()
            )
        }
        "Désolé, j'ai eu besoin de trop d'étapes pour répondre à cette demande."
    }
}
