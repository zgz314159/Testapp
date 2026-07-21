package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.component.stepper.CapsuleStepperInput
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import kotlin.math.roundToInt

private const val FONT_SIZE_MIN = 14
private const val FONT_SIZE_MAX = 32

@Composable
fun SettingsAppearanceCard(
    fontSize: Float,
    fontStyle: String,
    soundEnabled: Boolean,
    darkTheme: Boolean,
    onFontSizeChange: (Float) -> Unit,
    onFontStyleChange: (String) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    val styleOptions = listOf(
        stringResource(R.string.font_style_normal),
        stringResource(R.string.font_style_serif),
        stringResource(R.string.font_style_monospace),
    )
    val styleValues = listOf("Normal", "Serif", "Monospace")
    val selectedStyleIndex = styleValues.indexOf(fontStyle).coerceAtLeast(0)
    val discreteSize = fontSize.roundToInt().coerceIn(FONT_SIZE_MIN, FONT_SIZE_MAX)
    val previewFamily = when (fontStyle) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    SettingsCardGroup {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsElevatedLeadingIcon(icon = Icons.Filled.TextFields)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_typography_section),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textPrimary,
                )
                Text(
                    text = stringResource(
                        R.string.settings_typography_summary,
                        discreteSize,
                        styleOptions[selectedStyleIndex],
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.textSecondary,
                )
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = tokens.brandBlueSoft,
                tonalElevation = 1.dp,
                shadowElevation = 4.dp,
            ) {
                Text(
                    text = "Aa",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = discreteSize.sp,
                    fontFamily = previewFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.brandBlue,
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(18.dp),
            color = tokens.sheetBg,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.font_size_label_short),
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = tokens.textSecondary,
                    )
                    CapsuleStepperInput(
                        value = discreteSize,
                        onValueChange = { onFontSizeChange(it.toFloat()) },
                        minValue = FONT_SIZE_MIN,
                        maxValue = FONT_SIZE_MAX,
                        formatDisplay = { "$it" },
                        contentDescription = stringResource(R.string.font_size_label_short),
                        width = 124,
                    )
                }
                SettingsSegmentedControl(
                    options = styleOptions,
                    selectedIndex = selectedStyleIndex,
                    onSelected = { onFontStyleChange(styleValues[it]) },
                )
            }
        }

        SettingsCardDivider()
        SettingsListSwitchRow(
            label = stringResource(R.string.dark_mode_label_short),
            fontSize = fontSize,
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange,
            leadingIcon = Icons.Filled.DarkMode,
        )
        SettingsCardDivider()
        SettingsListSwitchRow(
            label = stringResource(R.string.sound_label_short),
            fontSize = fontSize,
            checked = soundEnabled,
            onCheckedChange = onSoundEnabledChange,
            leadingIcon = Icons.AutoMirrored.Filled.VolumeUp,
        )
    }
}
