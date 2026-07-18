package com.example.testapp.presentation.screen.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.QuestionTypographyBounds
import com.example.testapp.uicommon.design.QuestionTypographyStepperRow
import com.example.testapp.uicommon.design.fontSizeToStep
import com.example.testapp.uicommon.design.stepToFontSize
import kotlinx.coroutines.launch
import com.example.testapp.uicommon.R as UiCommonR

/** AI 问答页右上角三点入口：打开与答题页同款的字体设置底部弹层。 */
@Composable
fun AiAskFontMenu(
    screenFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    fontSizeStore: suspend (Float) -> Unit,
    settingsLabel: String,
) {
    val coroutineScope = rememberCoroutineScope()
    var sheetVisible by remember { mutableStateOf(false) }
    IconButton(onClick = { sheetVisible = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = settingsLabel)
    }
    AiAskTypographySheet(
        visible = sheetVisible,
        fontSize = screenFontSize,
        onFontSizeChange = { next ->
            onFontSizeChange(next)
            coroutineScope.launch { fontSizeStore(next) }
        },
        onDismiss = { sheetVisible = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiAskTypographySheet(
    visible: Boolean,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val tokens = AppElevatedActionSheetTokens
    val maxFontStep = (QuestionTypographyBounds.FONT_MAX - QuestionTypographyBounds.FONT_MIN) /
        QuestionTypographyBounds.FONT_STEP
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
                text = stringResource(UiCommonR.string.uicommon_typography_settings_title),
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
            Spacer(modifier = Modifier.height(18.dp))
            QuestionTypographyStepperRow(
                label = stringResource(UiCommonR.string.uicommon_typography_font_size),
                step = fontSizeToStep(fontSize),
                onStepChange = { onFontSizeChange(stepToFontSize(it)) },
                minStep = 0,
                maxStep = maxFontStep,
                formatDisplay = { stepToFontSize(it).toInt().toString() },
            )
        }
    }
}
