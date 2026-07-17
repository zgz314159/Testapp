package com.example.testapp.uicommon.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.uicommon.R

@Composable
fun PracticeExamAiDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    deepSeekLabel: String,
    sparkLabel: String,
    onDeepSeek: () -> Unit,
    onSparkAsk: () -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    AppElevatedActionSheet(
        visible = expanded,
        title = stringResource(R.string.uicommon_ai_action_sheet_title),
        subtitle = stringResource(R.string.uicommon_ai_action_sheet_subtitle),
        onDismiss = onDismiss,
        actions = listOf(
            AppElevatedActionItem(
                title = deepSeekLabel,
                subtitle = stringResource(R.string.uicommon_ai_deepseek_hint),
                icon = iconForPracticeExamAiMenuAction(PracticeExamAiMenuAction.DeepSeek),
                iconTint = tokens.brandBlue,
                iconBg = tokens.brandBlueSoft,
                onClick = {
                    onDismiss()
                    onDeepSeek()
                },
            ),
            AppElevatedActionItem(
                title = sparkLabel,
                subtitle = stringResource(R.string.uicommon_ai_spark_hint),
                icon = iconForPracticeExamAiMenuAction(PracticeExamAiMenuAction.SparkAsk),
                iconTint = tokens.accentPurple,
                iconBg = tokens.accentPurpleSoft,
                onClick = {
                    onDismiss()
                    onSparkAsk()
                },
            ),
        ),
    )
}
