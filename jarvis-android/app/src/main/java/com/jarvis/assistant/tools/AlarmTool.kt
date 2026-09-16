package com.jarvis.assistant.tools

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

class AlarmTool(private val context: Context) {

    fun setAlarm(hour: Int, minute: Int, label: String?): String {
        if (hour !in 0..23 || minute !in 0..59) {
            return "Heure invalide : $hour:$minute."
        }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            if (!label.isNullOrBlank()) putExtra(AlarmClock.EXTRA_MESSAGE, label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            "Alarme réglée à %02d:%02d%s.".format(hour, minute, if (!label.isNullOrBlank()) " (\"$label\")" else "")
        } catch (e: Exception) {
            "Impossible de régler l'alarme : aucune application d'horloge compatible trouvée."
        }
    }

    fun openAlarmsList(): String {
        context.startActivity(
            Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return "Liste des alarmes ouverte."
    }
}
