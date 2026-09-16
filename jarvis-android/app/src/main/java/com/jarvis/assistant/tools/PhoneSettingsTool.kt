package com.jarvis.assistant.tools

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.jarvis.assistant.data.PermissionsHelper

/**
 * Modern Android deliberately blocks silent, programmatic control of Wi-Fi, Bluetooth
 * and Airplane mode for regular apps (privacy hardening since Android 10/13). Where a
 * direct API call is not possible, we open the right system panel so the user finishes
 * the action with one tap instead of hunting through Settings themselves.
 */
class PhoneSettingsTool(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun setVolume(percent: Int): String {
        val clamped = percent.coerceIn(0, 100)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (max * clamped / 100.0).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        return "Volume réglé à $clamped %."
    }

    fun setBrightness(percent: Int): String {
        if (!PermissionsHelper.canWriteSystemSettings(context)) {
            openPanel(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            return "Autorisation « Modifier les paramètres système » requise - écran d'autorisation ouvert."
        }
        val clamped = percent.coerceIn(1, 100)
        val value = (255 * clamped / 100.0).toInt()
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        return "Luminosité réglée à $clamped %."
    }

    fun openWifiPanel(): String {
        openPanel(Settings.Panel.ACTION_WIFI)
        return "Panneau Wi-Fi ouvert - Android ne permet plus aux apps de basculer le Wi-Fi automatiquement, un geste suffit."
    }

    fun openBluetoothPanel(): String {
        if (Build.VERSION.SDK_INT >= 31 &&
            PermissionsHelper.has(context, android.Manifest.permission.BLUETOOTH_CONNECT)
        ) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null && !adapter.isEnabled) {
                context.startActivity(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return "Demande d'activation du Bluetooth envoyée."
            }
        }
        openPanel(Settings.ACTION_BLUETOOTH_SETTINGS)
        return "Écran des réglages Bluetooth ouvert (Android ne permet plus à une app de le basculer directement)."
    }

    fun openAirplaneModeSettings(): String {
        context.startActivity(
            Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return "Écran du mode avion ouvert - seul le système peut le basculer, pas les apps tierces."
    }

    private fun openPanel(action: String) {
        context.startActivity(
            Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
