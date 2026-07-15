package com.example.testapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4F8CFF),
    primaryContainer = Color(0xFF1A3A7A),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF79C9FF),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1D22),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF242830),
    onSurfaceVariant = Color(0xFFC2C7D0),
    outline = Color(0xFF3A3D45),
)

private val LightColorScheme = lightColorScheme(
    primary = SettingsPrimary,
    primaryContainer = BlueContainerLight,
    secondary = SettingsSecondary,
    background = SettingsBackground,
    surface = ReadingSurface,
    surfaceVariant = SettingsSurfaceVariant,
    onSurfaceVariant = SettingsOnSurfaceVariant,
    onBackground = ReadingOnSurface,
    onSurface = ReadingOnSurface,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
