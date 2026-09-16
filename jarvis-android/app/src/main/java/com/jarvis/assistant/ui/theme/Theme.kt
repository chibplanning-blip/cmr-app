package com.jarvis.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisBlue = Color(0xFF2196F3)
private val JarvisDark = Color(0xFF0D47A1)

private val DarkColors = darkColorScheme(primary = JarvisBlue, secondary = JarvisDark)
private val LightColors = lightColorScheme(primary = JarvisDark, secondary = JarvisBlue)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
