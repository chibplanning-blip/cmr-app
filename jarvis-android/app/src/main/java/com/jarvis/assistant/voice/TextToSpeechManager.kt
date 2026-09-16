package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

class TextToSpeechManager(context: Context) {

    private var ready = false
    private val tts = TextToSpeech(context) { status ->
        ready = status == TextToSpeech.SUCCESS
    }

    init {
        tts.language = Locale.FRANCE
    }

    /** Speaks [text] and suspends until playback finishes. */
    suspend fun speak(text: String) {
        if (!ready || text.isBlank()) return
        val utteranceId = UUID.randomUUID().toString()

        suspendCancellableCoroutine<Unit> { cont ->
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    if (cont.isActive) cont.resumeWith(Result.success(Unit))
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (cont.isActive) cont.resumeWith(Result.success(Unit))
                }
            })
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            cont.invokeOnCancellation { tts.stop() }
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
