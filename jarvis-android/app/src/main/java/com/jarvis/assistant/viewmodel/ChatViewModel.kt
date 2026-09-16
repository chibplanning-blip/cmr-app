package com.jarvis.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anthropic.models.messages.MessageParam
import com.jarvis.assistant.data.SecurePrefs
import com.jarvis.assistant.network.ClaudeClient
import com.jarvis.assistant.tools.ToolExecutor
import com.jarvis.assistant.voice.SpeechEvent
import com.jarvis.assistant.voice.SpeechToTextManager
import com.jarvis.assistant.voice.TextToSpeechManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val securePrefs = SecurePrefs(application)
    private val toolExecutor = ToolExecutor(application)
    private val speechToText = SpeechToTextManager(application)
    private val textToSpeech = TextToSpeechManager(application)

    private val history = mutableListOf<MessageParam>()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _state = MutableStateFlow(AssistantState.IDLE)
    val state: StateFlow<AssistantState> = _state

    private val _pendingConfirmation = MutableStateFlow<ConfirmationRequest?>(null)
    val pendingConfirmation: StateFlow<ConfirmationRequest?> = _pendingConfirmation

    private var pendingConfirmationAnswer: CompletableDeferred<Boolean>? = null
    private var listeningJob: Job? = null

    // True while the current exchange was started by voice - once true, Jarvis keeps
    // listening again automatically after each reply instead of waiting for another tap,
    // so the back-and-forth feels like a normal spoken conversation rather than a
    // press-to-talk walkie-talkie. Cleared as soon as the user types, or taps the mic
    // again to stop, or a listening attempt comes back empty/errors out.
    private var conversationModeActive = false

    fun hasApiKey(): Boolean = securePrefs.hasApiKey

    fun startListening() {
        if (_state.value == AssistantState.LISTENING) {
            stopListening()
            return
        }
        if (_state.value != AssistantState.IDLE) return

        conversationModeActive = true
        _state.value = AssistantState.LISTENING

        listeningJob = viewModelScope.launch {
            speechToText.listen().collect { event ->
                when (event) {
                    is SpeechEvent.FinalResult -> sendUserText(event.text)
                    is SpeechEvent.Error -> {
                        appendMessage(ChatRole.SYSTEM, event.message)
                        conversationModeActive = false
                        _state.value = AssistantState.IDLE
                    }
                    is SpeechEvent.Done -> if (_state.value == AssistantState.LISTENING) {
                        // Silence with nothing recognised: end the conversation loop quietly.
                        conversationModeActive = false
                        _state.value = AssistantState.IDLE
                    }
                    else -> Unit
                }
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        conversationModeActive = false
        _state.value = AssistantState.IDLE
    }

    /** Called from the text input - a typed message always breaks conversation mode. */
    fun sendTypedText(text: String) {
        conversationModeActive = false
        sendUserText(text)
    }

    fun sendUserText(text: String) {
        if (text.isBlank()) return
        appendMessage(ChatRole.USER, text)
        history.add(MessageParam.builder().role(MessageParam.Role.USER).content(text).build())

        val apiKey = securePrefs.apiKey
        if (apiKey.isNullOrBlank()) {
            appendMessage(
                ChatRole.SYSTEM,
                "Aucune clé API Anthropic configurée. Ouvre les paramètres pour en ajouter une."
            )
            conversationModeActive = false
            _state.value = AssistantState.IDLE
            return
        }

        _state.value = AssistantState.THINKING
        viewModelScope.launch {
            val client = ClaudeClient(apiKey, securePrefs.model)
            val reply = try {
                client.sendAndResolve(history, toolExecutor) { confirmationMessage ->
                    askUserToConfirm(confirmationMessage)
                }
            } catch (e: Exception) {
                "Erreur en contactant Claude : ${e.message}"
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
        _messages.value = _messages.value + ChatMessage(role, text)
    }

    override fun onCleared() {
        textToSpeech.shutdown()
        super.onCleared()
    }
}
