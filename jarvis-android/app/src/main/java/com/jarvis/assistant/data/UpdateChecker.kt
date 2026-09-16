package com.jarvis.assistant.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.jarvis.assistant.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

private const val RELEASE_API_URL =
    "https://api.github.com/repos/chibplanning-blip/cmr-app/releases/tags/jarvis-debug-latest"

data class UpdateInfo(val downloadUrl: String, val notes: String)

/**
 * Checks whether a newer Jarvis build than the one currently installed is published as a
 * GitHub release, using the public (unauthenticated) GitHub API - the repo is public, so no
 * token is needed, just a modest per-IP rate limit.
 */
class UpdateChecker(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** Returns null if this is already the latest build, or on any error (offline, rate limit, ...). */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(RELEASE_API_URL).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body?.string().orEmpty())
                val notes = json.optString("body", "")

                // The release notes embed the exact commit SHA that was built. If that SHA
                // is already the one baked into this APK, we're already on the latest build.
                if (notes.contains(BuildConfig.BUILD_SHA)) return@withContext null

                val assets = json.optJSONArray("assets") ?: return@withContext null
                if (assets.length() == 0) return@withContext null
                val downloadUrl = assets.getJSONObject(0).optString("browser_download_url")
                if (downloadUrl.isBlank()) return@withContext null

                UpdateInfo(downloadUrl, notes)
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Downloads the APK and hands it to the system installer. Suspends until the download finishes. */
    suspend fun downloadAndInstall(update: UpdateInfo) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(update.downloadUrl).build()
        val apkFile = File(context.cacheDir, "jarvis-update.apk")

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Téléchargement échoué (${response.code}).")
            apkFile.outputStream().use { output ->
                response.body?.byteStream()?.copyTo(output)
                    ?: throw java.io.IOException("Réponse vide du serveur.")
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        withContext(Dispatchers.Main) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            )
        }
    }
}
