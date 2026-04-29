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

private val DeepViolet = Color(0xFF4F3CD6)
private val Fuchsia = Color(0xFFFF4D8D)

private val InkLight = Color(0xFF0C0C16)
private val InkDark = Color(0xFFEDEAF7)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFBFAEFF),
    onPrimary = Color(0xFF1A0E5C),
    primaryContainer = Color(0xFF3F2DB8),
    onPrimaryContainer = Color(0xFFE8E0FF),

    secondary = Color(0xFFFF99BC),
    onSecondary = Color(0xFF420A22),
    secondaryContainer = Color(0xFF8B1D4D),
    onSecondaryContainer = Color(0xFFFFD9E6),

    tertiary = Color(0xFFFFC58B),
    onTertiary = Color(0xFF4A2A00),
    tertiaryContainer = Color(0xFF6E3F00),
    onTertiaryContainer = Color(0xFFFFE2C7),

    background = Color(0xFF09091A),
    onBackground = InkDark,
    surface = Color(0xFF12121F),
    onSurface = InkDark,
    surfaceVariant = Color(0xFF1D1D2D),
    onSurfaceVariant = Color(0xFFC9C5DC),
    surfaceTint = Color(0xFFBFAEFF),

    outline = Color(0xFF49495F),
    outlineVariant = Color(0xFF2A2A3C),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightScheme = lightColorScheme(
    primary = DeepViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF180C57),

    secondary = Fuchsia,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE2EE),
    onSecondaryContainer = Color(0xFF44081F),

    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE3C2),
    onTertiaryContainer = Color(0xFF341900),

    background = Color(0xFFF7F6FB),
    onBackground = InkLight,
    surface = Color.White,
    onSurface = InkLight,
    surfaceVariant = Color(0xFFEFEDF7),
    onSurfaceVariant = Color(0xFF4B4A5E),
    surfaceTint = DeepViolet,

    outline = Color(0xFFCAC7D6),
    outlineVariant = Color(0xFFE3E1EE),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
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
            context.window.statusBarColor = Color.Transparent.toArgb()
            context.window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(context.window, false)
            val controller = WindowCompat.getInsetsController(context.window, context.window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = PulsifyShapes,
        content = content,
    )
}
