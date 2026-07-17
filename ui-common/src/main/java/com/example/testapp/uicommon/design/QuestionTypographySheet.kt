package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTypographySheet(
    visible: Boolean,
    fontSize: Float,
    lineSpacing: Float,
    letterSpacing: Float,
    onFontSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onLetterSpacingChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val fontStep = fontSizeToStep(fontSize)
    val lineTenths = lineSpacingToTenths(lineSpacing)
    val letterTenths = letterSpacingToTenths(letterSpacing)
    val maxFontStep = (QuestionTypographyBounds.FONT_MAX - QuestionTypographyBounds.FONT_MIN) /
        QuestionTypographyBounds.FONT_STEP
    val tokens = AppElevatedActionSheetTokens
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = tokens.sheetCorner, topEnd = tokens.sheetCorner),
        containerColor = tokens.sheetBg,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.uicommon_typography_settings_title),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textPrimary,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color(0x291B2B4E),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f,
                    ),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.uicommon_typography_hint),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = tokens.textSecondary,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                QuestionTypographyStepperRow(
                    label = stringResource(R.string.uicommon_typography_font_size),
                    step = fontStep,
                    onStepChange = { onFontSizeChange(stepToFontSize(it)) },
                    minStep = 0,
                    maxStep = maxFontStep,
                    formatDisplay = { stepToFontSize(it).toInt().toString() },
                )
                QuestionTypographyStepperRow(
                    label = stringResource(R.string.uicommon_typography_line_spacing),
                    step = lineTenths,
                    onStepChange = { onLineSpacingChange(tenthsToLineSpacing(it)) },
                    minStep = QuestionTypographyBounds.LINE_SPACING_MIN_TENTHS,
                    maxStep = QuestionTypographyBounds.LINE_SPACING_MAX_TENTHS,
                    formatDisplay = { formatLineSpacingDisplay(tenthsToLineSpacing(it)) },
                )
                QuestionTypographyStepperRow(
                    label = stringResource(R.string.uicommon_typography_letter_spacing),
                    step = letterTenths,
                    onStepChange = { onLetterSpacingChange(tenthsToLetterSpacing(it)) },
                    minStep = 0,
                    maxStep = QuestionTypographyBounds.LETTER_SPACING_MAX_TENTHS,
                    formatDisplay = { formatLetterSpacingDisplay(tenthsToLetterSpacing(it)) },
                )
            }
        }
    }
}
