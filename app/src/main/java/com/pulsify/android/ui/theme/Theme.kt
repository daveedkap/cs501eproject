package com.pulsify.android.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

private val Teal = Color(0xFF0F766E)
private val Coral = Color(0xFFFF6B6B)
private val Ink = Color(0xFF0B1220)
private val Mist = Color(0xFFE8F4F2)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Ink,
    secondary = Coral,
    onSecondary = Color.White,
    tertiary = Color(0xFF93C5FD),
    background = Ink,
    surface = Color(0xFF111827),
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

private val LightScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    tertiary = Color(0xFF2563EB),
    background = Mist,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Color(0xFFD1E7E3),
    onSurfaceVariant = Color(0xFF334155),
)

@Composable
fun PulsifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val context = androidx.compose.ui.platform.LocalContext.current
    if (context is ComponentActivity) {
        SideEffect {
            context.window.statusBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(context.window, context.window.decorView)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content,
    )
}
