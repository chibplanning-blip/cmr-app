package com.jarvis.assistant.tools

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

data class CapturedNotification(
    val appLabel: String,
    val title: String,
    val text: String,
    val timestampMillis: Long
)

/**
 * In-memory buffer of recent notifications captured by JarvisNotificationListenerService.
 * Cleared when the process dies - nothing is persisted to disk.
 */
object NotificationRepository {

    private const val MAX_SIZE = 50
    private val buffer = ConcurrentLinkedDeque<CapturedNotification>()

    fun add(notification: CapturedNotification) {
        buffer.addFirst(notification)
        while (buffer.size > MAX_SIZE) {
            buffer.removeLast()
        }
    }

    fun recent(count: Int): List<CapturedNotification> =
        buffer.take(count.coerceIn(1, MAX_SIZE))

    fun formatForModel(count: Int): String {
        val items = recent(count)
        if (items.isEmpty()) return "Aucune notification récente disponible."
        val fmt = SimpleDateFormat("HH:mm", Locale.FRANCE)
        return items.joinToString("\n") { n ->
            "[${fmt.format(n.timestampMillis)}] ${n.appLabel} — ${n.title}: ${n.text}"
        }
    }
}
