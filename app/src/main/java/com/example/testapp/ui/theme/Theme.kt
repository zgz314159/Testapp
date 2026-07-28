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
    primary = Color(0xFF78A7FF),
    onPrimary = Color(0xFF072A5D),
    primaryContainer = Color(0xFF173A70),
    onPrimaryContainer = Color(0xFFD7E5FF),
    secondary = Color(0xFF79C9FF),
    onSecondary = Color(0xFF00344D),
    secondaryContainer = Color(0xFF174A61),
    onSecondaryContainer = Color(0xFFC9ECFF),
    background = Color(0xFF0F1012),
    onBackground = Color(0xFFF2F2F3),
    surface = Color(0xFF1B1C1F),
    onSurface = Color(0xFFF2F2F3),
    surfaceVariant = Color(0xFF25262A),
    onSurfaceVariant = Color(0xFFB8BBC2),
    surfaceContainerLowest = Color(0xFF0A0B0D),
    surfaceContainerLow = Color(0xFF151619),
    surfaceContainer = Color(0xFF1B1C1F),
    surfaceContainerHigh = Color(0xFF222327),
    surfaceContainerHighest = Color(0xFF292A2F),
    outline = Color(0xFF3A3C42),
    outlineVariant = Color(0xFF2D2F34),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF5B2023),
    onErrorContainer = Color(0xFFFFDAD6),
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
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
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
        content = content,
    )
}
