package com.jarvis.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.AiProvider
import com.jarvis.assistant.data.SecurePrefs
import com.jarvis.assistant.network.AiClient
import com.jarvis.assistant.network.ClaudeClient
import com.jarvis.assistant.network.GeminiClient
import com.jarvis.assistant.tools.ToolExecutor
import com.jarvis.assistant.voice.SpeechEvent
import com.jarvis.assistant.voice.SpeechToTextManager
import com.jarvis.assistant.voice.TextToSpeechManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val MAX_PERSISTED_MESSAGES = 100

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val securePrefs = SecurePrefs(application)
    private val toolExecutor = ToolExecutor(application)
    private val speechToText = SpeechToTextManager(application)
    private val textToSpeech = TextToSpeechManager(application)

    // Recreated only when the provider/model/key actually changes, so the same AiClient
    // (and its internal conversation history) is reused across turns of one conversation.
    private var aiClient: AiClient? = null
    private var aiClientSignature: String? = null

    // The visible transcript survives an app restart (loaded below); Jarvis's own working
    // memory of facts about the user (remember_fact tool) survives separately and forever -
    // the AI conversation history itself is not replayed into a fresh AiClient on restart.
    private val _messages = MutableStateFlow(loadPersistedMessages(securePrefs))
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _state = MutableStateFlow(AssistantState.IDLE)
    val state: StateFlow<AssistantState> = _state

    private val _pendingConfirmation = MutableStateFlow<ConfirmationRequest?>(null)
    val pendingConfirmation: StateFlow<ConfirmationRequest?> = _pendingConfirmation

    private var pendingConfirmationAnswer: CompletableDeferred<Boolean>? = null
    private var listeningJob: Job? = null
    private var replyJob: Job? = null

    // True while the current exchange was started by voice - once true, Jarvis keeps
    // listening again automatically after each reply instead of waiting for another tap,
    // so the back-and-forth feels like a normal spoken conversation rather than a
    // press-to-talk walkie-talkie. Cleared as soon as the user types, or taps the mic
    // again to stop, or a listening attempt comes back empty/errors out.
    private var conversationModeActive = false

    fun hasApiKey(): Boolean = securePrefs.hasApiKey

    /** Cancels whatever Jarvis is currently doing (thinking, speaking, listening) and resets. */
    fun cancelCurrent() {
        listeningJob?.cancel()
        listeningJob = null
        replyJob?.cancel()
        replyJob = null
        pendingConfirmationAnswer?.complete(false)
        pendingConfirmationAnswer = null
        _pendingConfirmation.value = null
        conversationModeActive = false
        textToSpeech.stop()
        _state.value = AssistantState.IDLE
    }

    fun startListening() {
        if (_state.value != AssistantState.IDLE) {
            cancelCurrent()
            return
        }

        conversationModeActive = true
        _state.value = AssistantState.LISTENING

        listeningJob = viewModelScope.launch {
            speechToText.listen().collect { event ->
                when (event) {
                    is SpeechEvent.FinalResult -> {
                        val heard = event.text.trim()
                        if (isStopWord(heard)) {
                            cancelCurrent()
                        } else {
                            sendUserText(heard)
                        }
                    }
                    is SpeechEvent.Error -> {
                        appendMessage(ChatRole.SYSTEM, event.message)
                        conversationModeActive = false
                        _state.value = AssistantState.IDLE
                    }
                    is SpeechEvent.Done -> if (_state.value == AssistantState.LISTENING) {
                        if (conversationModeActive) {
                            // Continuous hands-free mode: silence just means listen again,
                            // not the end of the conversation - only "stop" or an error does.
                            _state.value = AssistantState.IDLE
                            startListening()
                        } else {
                            _state.value = AssistantState.IDLE
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    /** Starts hands-free mode: Jarvis listens continuously until you say "stop". */
    fun startHandsFree() = startListening()

    private fun isStopWord(text: String): Boolean {
        val normalized = text.trim().trimEnd('.', '!', '?').lowercase()
        return normalized == "stop" || normalized == "stoppe" || normalized == "arrête" || normalized == "arrête-toi"
    }

    /** Called from the text input - a typed message always breaks conversation mode. */
    fun sendTypedText(text: String) {
        conversationModeActive = false
        sendUserText(text)
    }

    fun sendUserText(text: String) {
        if (text.isBlank()) return
        appendMessage(ChatRole.USER, text)

        val apiKey = securePrefs.activeApiKey
        if (apiKey.isNullOrBlank()) {
            appendMessage(
                ChatRole.SYSTEM,
                "Aucune clé API configurée pour ce fournisseur. Ouvre les paramètres pour en ajouter une."
            )
            conversationModeActive = false
            _state.value = AssistantState.IDLE
            return
        }

        val client = currentAiClient(apiKey)
        val providerName = if (securePrefs.provider == AiProvider.CLAUDE) "Claude" else "Gemini"

        _state.value = AssistantState.THINKING
        replyJob = viewModelScope.launch {
            val reply = try {
                client.sendMessage(text, toolExecutor) { confirmationMessage ->
                    askUserToConfirm(confirmationMessage)
                }
            } catch (e: Exception) {
                "Erreur en contactant $providerName : ${e.message}"
            }

            appendMessage(ChatRole.ASSISTANT, reply)
            _state.value = AssistantState.SPEAKING
            textToSpeech.speak(reply)

            if (conversationModeActive) {
                // Fluid conversation: go straight back to listening, no button press needed.
                _state.value = AssistantState.IDLE
                startListening()
            } else {
                _state.value = AssistantState.IDLE
            }
        }
    }

    /**
     * Reuses the existing AiClient (and its conversation history) unless the provider,
     * model, or key changed since it was created - a real provider switch always starts
     * a fresh conversation, since Claude and Gemini can't share tool-call history anyway.
     */
    private fun currentAiClient(apiKey: String): AiClient {
        val provider = securePrefs.provider
        val model = securePrefs.activeModel
        val signature = "$provider:$model:$apiKey"

        if (aiClient == null || aiClientSignature != signature) {
            aiClient = when (provider) {
                AiProvider.CLAUDE -> ClaudeClient(apiKey, model, securePrefs)
                AiProvider.GEMINI -> GeminiClient(apiKey, model, securePrefs)
            }
            aiClientSignature = signature
        }
        return aiClient!!
    }

    private suspend fun askUserToConfirm(message: String): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingConfirmationAnswer = deferred
        _pendingConfirmation.value = ConfirmationRequest(message)
        val answer = deferred.await()
        _pendingConfirmation.value = null
        return answer
    }

    fun answerConfirmation(approved: Boolean) {
        pendingConfirmationAnswer?.complete(approved)
        pendingConfirmationAnswer = null
    }

    private fun appendMessage(role: ChatRole, text: String) {
        val updated = (_messages.value + ChatMessage(role, text)).takeLast(MAX_PERSISTED_MESSAGES)
        _messages.value = updated
        persistMessages(updated)
    }

    private fun persistMessages(messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(JSONObject().put("role", message.role.name).put("text", message.text))
        }
        securePrefs.chatHistoryJson = array.toString()
    }

    /** Erases the visible transcript and everything Jarvis has remembered - a fresh start. */
    fun forgetEverything() {
        securePrefs.forgetEverything()
        _messages.value = emptyList()
        aiClient = null
        aiClientSignature = null
    }

    override fun onCleared() {
        textToSpeech.shutdown()
        super.onCleared()
    }
}

private fun loadPersistedMessages(securePrefs: SecurePrefs): List<ChatMessage> {
    val raw = securePrefs.chatHistoryJson ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { i ->
            val obj = array.optJSONObject(i) ?: return@mapNotNull null
            val role = try {
                ChatRole.valueOf(obj.optString("role"))
            } catch (e: IllegalArgumentException) {
                ChatRole.SYSTEM
            }
            ChatMessage(role, obj.optString("text"))
        }
    } catch (e: Exception) {
        emptyList()
    }
}
