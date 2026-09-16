package com.jarvis.assistant.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import com.jarvis.assistant.data.PermissionsHelper

/**
 * Calling and texting are irreversible, real-world actions - the ViewModel always
 * gets user confirmation before invoking these, regardless of what the model asks for.
 */
class PhoneActionsTool(private val context: Context) {

    private val contacts = ContactsTool(context)

    fun call(nameOrNumber: String): String {
        val number = resolveNumber(nameOrNumber) ?: return "Contact « $nameOrNumber » introuvable."

        return if (PermissionsHelper.has(context, android.Manifest.permission.CALL_PHONE)) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Appel en cours vers $number."
        } else {
            // No CALL_PHONE permission: fall back to opening the dialer pre-filled,
            // the user taps the call button themselves.
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Numéroteur ouvert avec $number (permission d'appel non accordée)."
        }
    }

    fun sendSms(nameOrNumber: String, message: String): String {
        val number = resolveNumber(nameOrNumber) ?: return "Contact « $nameOrNumber » introuvable."

        return if (PermissionsHelper.has(context, android.Manifest.permission.SEND_SMS)) {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(number, null, message, null, null)
            "SMS envoyé à $number."
        } else {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Application SMS ouverte avec le message pré-rempli pour $number (permission d'envoi non accordée)."
        }
    }

    private fun resolveNumber(nameOrNumber: String): String? {
        if (nameOrNumber.any { it.isDigit() } && nameOrNumber.count { it.isDigit() } >= 4) {
            return nameOrNumber
        }
        return contacts.findByName(nameOrNumber)?.number
    }
}
