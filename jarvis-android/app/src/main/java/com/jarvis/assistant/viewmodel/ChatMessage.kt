package com.jarvis.assistant.viewmodel

enum class ChatRole { USER, ASSISTANT, SYSTEM }

data class ChatMessage(val role: ChatRole, val text: String)

enum class AssistantState { IDLE, LISTENING, THINKING, SPEAKING }

data class ConfirmationRequest(val message: String)
