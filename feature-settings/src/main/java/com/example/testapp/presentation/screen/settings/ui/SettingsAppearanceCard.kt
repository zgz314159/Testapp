package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceCard(
    fontSize: Float,
    fontStyle: String,
    soundEnabled: Boolean,
    darkTheme: Boolean,
    onFontSizeChange: (Float) -> Unit,
    onFontStyleChange: (String) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val styleOptions = listOf(
        stringResource(R.string.font_style_normal),
        stringResource(R.string.font_style_serif),
        stringResource(R.string.font_style_monospace)
    )
    val styleValues = listOf("Normal", "Serif", "Monospace")
    val selectedStyleIndex = styleValues.indexOf(fontStyle).coerceAtLeast(0)

    SettingsCardGroup {
        SettingsListSliderRow(
            label = stringResource(R.string.font_size_label_short),
            fontSize = fontSize,
            value = fontSize,
            valueRange = 14f..32f,
            onValueChange = onFontSizeChange,
            leadingIcon = Icons.Filled.FormatSize,
            valueLabel = { "${it.toInt()}" },
            showRangeLabels = true,
            rangeMinLabel = "14",
            rangeMaxLabel = "32"
        )
        SettingsCardDivider()
        SettingsSegmentedChoiceRow(
            label = stringResource(R.string.font_style_label_short),
            fontSize = fontSize,
            options = styleOptions,
            selectedIndex = selectedStyleIndex,
            onSelectedIndexChange = { onFontStyleChange(styleValues[it]) },
            leadingIcon = Icons.Filled.TextFormat
        )
        SettingsCardDivider()
        SettingsListSwitchRow(
            label = stringResource(R.string.dark_mode_label_short),
            fontSize = fontSize,
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange,
            leadingIcon = Icons.Filled.DarkMode
        )
        SettingsCardDivider()
        SettingsListSwitchRow(
            label = stringResource(R.string.sound_label_short),
            fontSize = fontSize,
            checked = soundEnabled,
            onCheckedChange = onSoundEnabledChange,
            leadingIcon = Icons.AutoMirrored.Filled.VolumeUp
        )
    }
}
