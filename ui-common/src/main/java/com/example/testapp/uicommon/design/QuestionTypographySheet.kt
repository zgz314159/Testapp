package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.testapp.uicommon.R

@Composable
fun QuestionTypographySheet(
    visible: Boolean,
    fontSize: Float,
    lineSpacing: Float,
    letterSpacing: Float,
    onFontSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onLetterSpacingChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val fontStep = fontSizeToStep(fontSize)
    val lineTenths = lineSpacingToTenths(lineSpacing)
    val letterTenths = letterSpacingToTenths(letterSpacing)
    val maxFontStep = (QuestionTypographyBounds.FONT_MAX - QuestionTypographyBounds.FONT_MIN) /
        QuestionTypographyBounds.FONT_STEP

    AppStaticBottomSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.uicommon_typography_settings_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.lg))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            QuestionTypographyStepperRow(
                label = stringResource(R.string.uicommon_typography_font_size),
                step = fontStep,
                onStepChange = { onFontSizeChange(stepToFontSize(it)) },
                minStep = 0,
                maxStep = maxFontStep,
                formatDisplay = { stepToFontSize(it).toInt().toString() }
            )
            QuestionTypographyStepperRow(
                label = stringResource(R.string.uicommon_typography_line_spacing),
                step = lineTenths,
                onStepChange = { onLineSpacingChange(tenthsToLineSpacing(it)) },
                minStep = QuestionTypographyBounds.LINE_SPACING_MIN_TENTHS,
                maxStep = QuestionTypographyBounds.LINE_SPACING_MAX_TENTHS,
                formatDisplay = { formatLineSpacingDisplay(tenthsToLineSpacing(it)) }
            )
            QuestionTypographyStepperRow(
                label = stringResource(R.string.uicommon_typography_letter_spacing),
                step = letterTenths,
                onStepChange = { onLetterSpacingChange(tenthsToLetterSpacing(it)) },
                minStep = 0,
                maxStep = QuestionTypographyBounds.LETTER_SPACING_MAX_TENTHS,
                formatDisplay = { formatLetterSpacingDisplay(tenthsToLetterSpacing(it)) }
            )
        }
    }
}
