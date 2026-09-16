package com.jarvis.assistant.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class AppLauncherTool(private val context: Context) {

    private data class LaunchableApp(val label: String, val packageName: String)

    private fun installedApps(): List<LaunchableApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(mainIntent, 0).map {
            LaunchableApp(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName
            )
        }
    }

    /** Fuzzy-matches [appName] against installed app labels, launches the best match. */
    fun launch(appName: String): String {
        val apps = installedApps()
        val query = appName.trim().lowercase()

        val exact = apps.firstOrNull { it.label.lowercase() == query }
        val contains = apps.firstOrNull { it.label.lowercase().contains(query) }
        val match = exact ?: contains

        if (match == null) {
            val suggestions = apps.map { it.label }.distinct().take(8).joinToString(", ")
            return "Aucune application nommée « $appName » trouvée. Apps installées (extrait) : $suggestions"
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(match.packageName)
            ?: return "« ${match.label} » a été trouvée mais ne peut pas être lancée."

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return "« ${match.label} » a été ouverte."
    }

    fun listApps(limit: Int = 30): String =
        installedApps().map { it.label }.distinct().sorted().take(limit).joinToString(", ")
}
