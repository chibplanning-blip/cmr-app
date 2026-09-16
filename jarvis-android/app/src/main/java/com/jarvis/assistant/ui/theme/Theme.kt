package com.jarvis.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A HUD/arc-reactor palette - always dark, never follows system light mode:
// the whole point of Jarvis is to feel like a glowing cockpit readout.
val JarvisVoid = Color(0xFF05080B)
val JarvisPanel = Color(0xFF0D1620)
val JarvisPanelLight = Color(0xFF152230)
val JarvisCyan = Color(0xFF31E8FF)
val JarvisCyanDim = Color(0xFF14495A)
val JarvisAmber = Color(0xFFFFB347)
val JarvisRed = Color(0xFFFF5C5C)

private val JarvisColors = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisVoid,
    secondary = JarvisAmber,
    onSecondary = JarvisVoid,
    background = JarvisVoid,
    onBackground = JarvisCyan,
    surface = JarvisPanel,
    onSurface = JarvisCyan,
    surfaceVariant = JarvisPanelLight,
    onSurfaceVariant = JarvisCyan,
    primaryContainer = JarvisCyanDim,
    onPrimaryContainer = JarvisCyan,
    secondaryContainer = JarvisPanelLight,
    onSecondaryContainer = JarvisAmber,
    errorContainer = Color(0xFF3A1414),
    onErrorContainer = JarvisRed,
    outline = JarvisCyanDim
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = JarvisColors, content = content)
}
