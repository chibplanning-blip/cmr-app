package com.jarvis.assistant.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.jarvis.assistant.tools.CapturedNotification
import com.jarvis.assistant.tools.NotificationRepository

/**
 * Requires the user to manually grant "Notification access" in system settings
 * (Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) - Android does not allow this
 * to be requested as a normal runtime permission.
 */
class JarvisNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == applicationContext.packageName) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val appLabel = try {
            val pm = applicationContext.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        NotificationRepository.add(
            CapturedNotification(
                appLabel = appLabel,
                title = title,
                text = text,
                timestampMillis = sbn.postTime
            )
        )
    }
}
