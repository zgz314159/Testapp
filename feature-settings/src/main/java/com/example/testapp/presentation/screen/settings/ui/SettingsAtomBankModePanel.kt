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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.SessionModeBadge
import com.example.testapp.uicommon.design.adaptiveFadingModeLabel

/**
 * 原子题库出题模式面板：三大类（填空题 / 自适应渐隐 / 记忆模式）。
 * 填空题下含原出题模式、规则、标签；自适应为独立会话，预留筛选扩展位。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsFillPanelContent(
    fontSize: Float,
    fillQuestionGenerationMode: FillQuestionGenerationMode,
    fillBlankCount: Int,
    fillFullAnswerRequireCorrect: Boolean,
    fillFullAnswerRandomOrder: Boolean,
    fillAnswerScoreMin: Int,
    fillAnswerScoreMax: Int,
    fillAnswerTagFilter: String,
    availableFillAnswerTags: List<String>,
    fillQuestionFilterSummary: FillQuestionFilterSummary,
    memoryExpanded: Boolean,
    memoryEnabled: Boolean,
    memoryBatchSize: Int,
    memoryWrongMode: Int,
    memoryPoolMode: Int,
    onModeChange: (FillQuestionGenerationMode) -> Unit,
    onBlankCountChange: (Int) -> Unit,
    onRequireCorrectChange: (Boolean) -> Unit,
    onRandomOrderChange: (Boolean) -> Unit,
    onScoreRangeChange: (Int, Int) -> Unit,
    onTagFilterChange: (String) -> Unit,
    onTagFilterClear: () -> Unit,
    onMemoryExpandedChange: (Boolean) -> Unit,
    onMemoryModeChange: (Boolean) -> Unit,
    onMemoryBatchSizeChange: (Int) -> Unit,
    onMemoryWrongModeChange: (Int) -> Unit,
    onMemoryPoolModeChange: (Int) -> Unit,
    showDetailedHelp: Boolean = false,
) {
    var fillExpanded by remember { mutableStateOf(true) }
    var adaptiveExpanded by remember { mutableStateOf(false) }

    SettingsSectionHeader(stringResource(R.string.fill_section_mode))
    SettingsCardGroup {
        SettingsExpandableCardSection(
            title = stringResource(R.string.fill_category_blank),
            fontSize = fontSize,
            expanded = fillExpanded,
            onExpandedChange = { fillExpanded = it },
            expandDescription = stringResource(R.string.expand_fill_blank),
            collapseDescription = stringResource(R.string.collapse_fill_blank),
            leadingIcon = Icons.Filled.EditNote,
        ) {
            SettingsFillBlankCategoryContent(
                fontSize = fontSize,
                mode = fillQuestionGenerationMode,
                fillBlankCount = fillBlankCount,
                fillFullAnswerRequireCorrect = fillFullAnswerRequireCorrect,
                fillFullAnswerRandomOrder = fillFullAnswerRandomOrder,
                fillAnswerScoreMin = fillAnswerScoreMin,
                fillAnswerScoreMax = fillAnswerScoreMax,
                fillAnswerTagFilter = fillAnswerTagFilter,
                availableFillAnswerTags = availableFillAnswerTags,
                fillQuestionFilterSummary = fillQuestionFilterSummary,
                onModeChange = onModeChange,
                onBlankCountChange = onBlankCountChange,
                onRequireCorrectChange = onRequireCorrectChange,
                onRandomOrderChange = onRandomOrderChange,
                onScoreRangeChange = onScoreRangeChange,
                onTagFilterChange = onTagFilterChange,
                onTagFilterClear = onTagFilterClear,
                showDetailedHelp = showDetailedHelp,
            )
        }
        SettingsCardDivider()
        SettingsExpandableCardSection(
            title = stringResource(R.string.fill_category_adaptive),
            fontSize = fontSize,
            expanded = adaptiveExpanded,
            onExpandedChange = { adaptiveExpanded = it },
            expandDescription = stringResource(R.string.expand_adaptive_fading),
            collapseDescription = stringResource(R.string.collapse_adaptive_fading),
            leadingIcon = Icons.Filled.AutoAwesome,
        ) {
            SettingsAdaptiveFadingCategoryContent(showDetailedHelp = showDetailedHelp)
        }
        SettingsCardDivider()
        SettingsMemoryCardSection(
            fontSize = fontSize,
            expanded = memoryExpanded,
            memoryEnabled = memoryEnabled,
            batchSize = memoryBatchSize,
            wrongMode = memoryWrongMode,
            poolMode = memoryPoolMode,
            onExpandedChange = onMemoryExpandedChange,
            onMemoryModeChange = onMemoryModeChange,
            onBatchSizeChange = onMemoryBatchSizeChange,
            onWrongModeChange = onMemoryWrongModeChange,
            onPoolModeChange = onMemoryPoolModeChange,
        )
    }
}

@Composable
private fun SettingsAdaptiveFadingCategoryContent(showDetailedHelp: Boolean) {
    val tokens = AppElevatedActionSheetTokens
    SettingsInsetPanel(modifier = Modifier.padding(top = 4.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = tokens.cardWhite,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SessionModeBadge(label = adaptiveFadingModeLabel())
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.fill_mode_adaptive_fading_note),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = tokens.textSecondary,
                    )
                    if (showDetailedHelp) {
                        Text(
                            text = stringResource(R.string.fill_adaptive_future_hint),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = tokens.brandBlue,
                        )
                    }
                }
            }
        }
    }
}
