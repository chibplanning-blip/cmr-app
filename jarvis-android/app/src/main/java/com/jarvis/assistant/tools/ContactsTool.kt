package com.jarvis.assistant.tools

import android.content.Context
import android.provider.ContactsContract

class ContactsTool(private val context: Context) {

    data class Contact(val name: String, val number: String)

    /** Best-effort fuzzy lookup by display name. Requires READ_CONTACTS. */
    fun findByName(name: String): Contact? {
        val query = name.trim().lowercase()
        if (query.isEmpty()) return null

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        ) ?: return null

        cursor.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val displayName = it.getString(nameIdx) ?: continue
                if (displayName.lowercase().contains(query)) {
                    return Contact(displayName, it.getString(numberIdx))
                }
            }
        }
        return null
    }
}
