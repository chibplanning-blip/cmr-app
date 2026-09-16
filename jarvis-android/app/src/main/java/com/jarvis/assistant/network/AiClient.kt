package com.jarvis.assistant.network

import com.jarvis.assistant.tools.ToolExecutor

/**
 * A conversational "brain" for Jarvis. Implementations own their own native conversation
 * history internally (Claude's MessageParam list, Gemini's Content list, ...) so the
 * ViewModel never has to know a provider's wire format - it just keeps talking to the
 * same AiClient instance for the life of one conversation.
 */
interface AiClient {
    suspend fun sendMessage(
        userText: String,
        toolExecutor: ToolExecutor,
        confirm: suspend (String) -> Boolean
    ): String
}
