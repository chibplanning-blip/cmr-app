package com.jarvis.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A HUD/arc-reactor palette - always dark, never follows system light mode:
// the whole point of Jarvis is to feel like a glowing cockpit readout. Body text uses a
// bright near-white (JarvisText), not the saturated accent cyan - a fully saturated color
// used for every line of text reads as too dark/low-contrast at normal font sizes.
val JarvisVoid = Color(0xFF0A0F14)
val JarvisPanel = Color(0xFF16222E)
val JarvisPanelLight = Color(0xFF203040)
val JarvisCyan = Color(0xFF4DEBFF)
val JarvisCyanDim = Color(0xFF2E6E82)
val JarvisAmber = Color(0xFFFFC46B)
val JarvisRed = Color(0xFFFF6B6B)
val JarvisText = Color(0xFFF2FBFF)

private val JarvisColors = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisVoid,
    secondary = JarvisAmber,
    onSecondary = JarvisVoid,
    background = JarvisVoid,
    onBackground = JarvisText,
    surface = JarvisPanel,
    onSurface = JarvisText,
    surfaceVariant = JarvisPanelLight,
    onSurfaceVariant = JarvisText,
    primaryContainer = JarvisCyanDim,
    onPrimaryContainer = JarvisText,
    secondaryContainer = JarvisPanelLight,
    onSecondaryContainer = JarvisAmber,
    errorContainer = Color(0xFF4A1A1A),
    onErrorContainer = JarvisRed,
    outline = JarvisCyan
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = JarvisColors, content = content)
}
