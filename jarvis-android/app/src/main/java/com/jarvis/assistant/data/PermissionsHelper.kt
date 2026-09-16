package com.jarvis.assistant.data

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Central place that knows what Jarvis is allowed to do right now.
 * Every tool call checks here before touching the phone.
 */
object PermissionsHelper {

    fun has(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun canReadNotifications(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.contains(context.packageName)
    }

    fun canWriteSystemSettings(context: Context): Boolean =
        Settings.System.canWrite(context)

    /** The list of dangerous runtime permissions Jarvis asks for up front. */
    val RUNTIME_PERMISSIONS = buildList {
        add(android.Manifest.permission.RECORD_AUDIO)
        add(android.Manifest.permission.CALL_PHONE)
        add(android.Manifest.permission.READ_CONTACTS)
        add(android.Manifest.permission.SEND_SMS)
        add(android.Manifest.permission.BLUETOOTH_CONNECT)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
