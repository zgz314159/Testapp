package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.AnswerTagFilterCodec
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

/**
 * 「填空题」大类展开内容：模式单选常驻，出题规则与标签侧重
 * 折叠为带当前值摘要的子区（渐进披露，避免整页平铺）。
 */
@Composable
fun SettingsFillBlankCategoryContent(
    fontSize: Float,
    mode: FillQuestionGenerationMode,
    fillBlankCount: Int,
    fillFullAnswerRequireCorrect: Boolean,
    fillFullAnswerRandomOrder: Boolean,
    fillAnswerScoreMin: Int,
    fillAnswerScoreMax: Int,
    fillAnswerTagFilter: String,
    availableFillAnswerTags: List<String>,
    fillQuestionFilterSummary: FillQuestionFilterSummary,
    onModeChange: (FillQuestionGenerationMode) -> Unit,
    onBlankCountChange: (Int) -> Unit,
    onRequireCorrectChange: (Boolean) -> Unit,
    onRandomOrderChange: (Boolean) -> Unit,
    onScoreRangeChange: (Int, Int) -> Unit,
    onTagFilterChange: (String) -> Unit,
    onTagFilterClear: () -> Unit,
    showDetailedHelp: Boolean,
) {
    var rulesExpanded by remember { mutableStateOf(false) }
    var tagsExpanded by remember { mutableStateOf(false) }
    val selectedTagCount = remember(fillAnswerTagFilter) {
        AnswerTagFilterCodec.decode(fillAnswerTagFilter).size
    }
    val tagsActive = mode.usesTagFilter

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        SettingsFillModeList(
            fontSize = fontSize,
            mode = mode,
            onModeChange = onModeChange,
            showDetailedHelp = showDetailedHelp,
        )
        SettingsCardDivider()
        SettingsExpandableCardSection(
            title = stringResource(R.string.fill_section_rules).trimLabelColon(),
            fontSize = fontSize,
            expanded = rulesExpanded,
            onExpandedChange = { rulesExpanded = it },
            expandDescription = stringResource(R.string.expand_fill_rules),
            collapseDescription = stringResource(R.string.collapse_fill_rules),
            supportingText = resolveOptionalCountStepperDescription(
                count = fillBlankCount,
                allLabel = stringResource(R.string.fill_blank_count_all),
                countedLabel = stringResource(R.string.fill_blank_count_template, fillBlankCount),
            ),
        ) {
            SettingsFillRulesCard(
                fontSize = fontSize,
                mode = mode,
                fillBlankCount = fillBlankCount,
                fillFullAnswerRequireCorrect = fillFullAnswerRequireCorrect,
                fillFullAnswerRandomOrder = fillFullAnswerRandomOrder,
                fillAnswerScoreMin = fillAnswerScoreMin,
                fillAnswerScoreMax = fillAnswerScoreMax,
                onBlankCountChange = onBlankCountChange,
                onRequireCorrectChange = onRequireCorrectChange,
                onRandomOrderChange = onRandomOrderChange,
                onScoreRangeChange = onScoreRangeChange,
                showDetailedHelp = showDetailedHelp,
            )
        }
        SettingsCardDivider()
        SettingsExpandableCardSection(
            title = stringResource(R.string.fill_section_tags).trimLabelColon(),
            fontSize = fontSize,
            expanded = tagsExpanded,
            onExpandedChange = { tagsExpanded = it },
            expandDescription = stringResource(R.string.expand_fill_tags),
            collapseDescription = stringResource(R.string.collapse_fill_tags),
            supportingText = when {
                !tagsActive -> stringResource(R.string.fill_tag_inactive_short)
                selectedTagCount > 0 -> stringResource(R.string.fill_tag_selected_count, selectedTagCount)
                else -> stringResource(R.string.fill_tag_none_selected)
            },
        ) {
            SettingsFillTagsCard(
                fontSize = fontSize,
                tagsActive = tagsActive,
                fillAnswerTagFilter = fillAnswerTagFilter,
                availableFillAnswerTags = availableFillAnswerTags,
                fillQuestionFilterSummary = fillQuestionFilterSummary,
                onTagFilterChange = onTagFilterChange,
                onTagFilterClear = onTagFilterClear,
                showDetailedHelp = showDetailedHelp,
            )
        }
    }
}

@Composable
private fun SettingsFillModeList(
    fontSize: Float,
    mode: FillQuestionGenerationMode,
    onModeChange: (FillQuestionGenerationMode) -> Unit,
    showDetailedHelp: Boolean,
) {
    val modeOptions = listOf(
        FillQuestionGenerationMode.SCORE_DESC to stringResource(R.string.fill_mode_score_desc),
        FillQuestionGenerationMode.SCORE_ASC to stringResource(R.string.fill_mode_score_asc),
        FillQuestionGenerationMode.TAG_RANDOM to stringResource(R.string.fill_mode_tag_random),
        FillQuestionGenerationMode.SCORE_RANGE_RANDOM to stringResource(R.string.fill_mode_score_range_random),
        FillQuestionGenerationMode.FULL_ANSWER to stringResource(R.string.fill_mode_full_answer),
    )
    val helpTextRes = when (mode) {
        FillQuestionGenerationMode.SCORE_DESC -> R.string.fill_mode_score_desc_help_text
        FillQuestionGenerationMode.SCORE_ASC -> R.string.fill_mode_score_asc_help_text
        FillQuestionGenerationMode.TAG_RANDOM -> R.string.fill_mode_tag_random_help_text
        FillQuestionGenerationMode.FULL_ANSWER -> R.string.fill_mode_full_answer_help_text
        else -> R.string.fill_mode_score_range_random_help_text
    }
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        modeOptions.forEach { (value, label) ->
            SettingsFillModeRow(
                label = label,
                selected = mode == value,
                onClick = { onModeChange(value) },
            )
        }
        if (showDetailedHelp) {
            SettingsHelpText(
                stringResource(helpTextRes),
                fontSize,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SettingsFillModeRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) tokens.brandBlueSoft else tokens.cardWhite,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                tokens.brandBlue.copy(alpha = 0.55f)
            } else {
                tokens.textSecondary.copy(alpha = 0.22f)
            },
        ),
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = if (selected) 5.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) tokens.brandBlue else tokens.textPrimary,
            )
            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) tokens.brandBlue else tokens.textSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SettingsFillRulesCard(
    fontSize: Float,
    mode: FillQuestionGenerationMode,
    fillBlankCount: Int,
    fillFullAnswerRequireCorrect: Boolean,
    fillFullAnswerRandomOrder: Boolean,
    fillAnswerScoreMin: Int,
    fillAnswerScoreMax: Int,
    onBlankCountChange: (Int) -> Unit,
    onRequireCorrectChange: (Boolean) -> Unit,
    onRandomOrderChange: (Boolean) -> Unit,
    onScoreRangeChange: (Int, Int) -> Unit,
    showDetailedHelp: Boolean,
) {
    val blankCountLabel = resolveOptionalCountStepperDescription(
        count = fillBlankCount,
        allLabel = stringResource(R.string.fill_blank_count_all),
        countedLabel = stringResource(R.string.fill_blank_count_template, fillBlankCount)
    )
    SettingsInsetPanel(modifier = Modifier.padding(top = 4.dp)) {
            SettingsInsetStepperRow(
                label = stringResource(R.string.fill_blank_count_label),
                contentDescription = blankCountLabel,
                value = fillBlankCount,
                onValueChange = onBlankCountChange,
                minValue = 0,
                maxValue = 11,
                formatDisplay = ::formatBlankCountDisplay,
            )
            if (showDetailedHelp) {
                SettingsHelpText(stringResource(R.string.fill_blank_help_text), fontSize)
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (mode == FillQuestionGenerationMode.FULL_ANSWER) {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsInsetLabel(
                    stringResource(R.string.fill_full_answer_completion_label).trimLabelColon()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSegmentedControl(
                    options = listOf(
                        stringResource(R.string.fill_full_answer_completion_answered),
                        stringResource(R.string.fill_full_answer_completion_correct),
                    ),
                    selectedIndex = if (fillFullAnswerRequireCorrect) 1 else 0,
                    onSelected = { onRequireCorrectChange(it == 1) },
                )
                if (showDetailedHelp) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsHelpText(stringResource(R.string.fill_full_answer_completion_help_text), fontSize)
                    SettingsHelpText(
                        stringResource(R.string.fill_full_answer_exam_completion_note),
                        fontSize,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                SettingsInsetLabel(
                    stringResource(R.string.fill_full_answer_order_label).trimLabelColon()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSegmentedControl(
                    options = listOf(
                        stringResource(R.string.fill_full_answer_order_random),
                        stringResource(R.string.fill_full_answer_order_sequential),
                    ),
                    selectedIndex = if (fillFullAnswerRandomOrder) 0 else 1,
                    onSelected = { onRandomOrderChange(it == 0) },
                )
                if (showDetailedHelp) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsHelpText(stringResource(R.string.fill_full_answer_order_help_text), fontSize)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (mode.usesScoreRange) {
                Spacer(modifier = Modifier.height(6.dp))
                val scoreLabel = stringResource(R.string.fill_answer_score_label).trimLabelColon()
                val scoreStepperDescriptions = resolveScoreRangeStepperDescriptions(
                    rangeLabel = scoreLabel,
                    minValue = fillAnswerScoreMin,
                    maxValue = fillAnswerScoreMax,
                    minTemplate = stringResource(R.string.settings_stepper_score_min),
                    maxTemplate = stringResource(R.string.settings_stepper_score_max)
                )
                SettingsScoreRangeStepperRow(
                    label = { SettingsInsetLabel(scoreLabel) },
                    minValue = fillAnswerScoreMin,
                    maxValue = fillAnswerScoreMax,
                    onRangeChange = onScoreRangeChange,
                    minContentDescription = scoreStepperDescriptions.min,
                    maxContentDescription = scoreStepperDescriptions.max
                )
                if (showDetailedHelp) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsHelpText(stringResource(R.string.fill_answer_score_help_text), fontSize)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsFillTagsCard(
    fontSize: Float,
    tagsActive: Boolean,
    fillAnswerTagFilter: String,
    availableFillAnswerTags: List<String>,
    fillQuestionFilterSummary: FillQuestionFilterSummary,
    onTagFilterChange: (String) -> Unit,
    onTagFilterClear: () -> Unit,
    showDetailedHelp: Boolean,
) {
    val tokens = AppElevatedActionSheetTokens
    val selectedTagTokens = remember(fillAnswerTagFilter) {
        AnswerTagFilterCodec.decode(fillAnswerTagFilter)
    }
    SettingsInsetPanel(modifier = Modifier.padding(top = 4.dp)) {
        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when {
                        !tagsActive -> stringResource(R.string.fill_tag_filter_inactive_hint)
                        selectedTagTokens.isEmpty() -> stringResource(R.string.fill_tag_none_selected)
                        else -> stringResource(R.string.fill_tag_selected_count, selectedTagTokens.size)
                    },
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (tagsActive) tokens.textSecondary else tokens.brandBlue,
                )
                if (tagsActive && selectedTagTokens.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.fill_answer_tag_filter_clear),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onTagFilterClear)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.brandBlue,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (availableFillAnswerTags.isNotEmpty()) {
                val visibleTags = availableFillAnswerTags.take(24)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (tagsActive) 1f else 0.45f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2,
                ) {
                    visibleTags.forEach { tag ->
                        val selected = tag in selectedTagTokens
                        SettingsTagTile(
                            label = tag,
                            selected = selected,
                            enabled = tagsActive,
                            onClick = {
                                val updated = if (selected) {
                                    selectedTagTokens.filterNot { it == tag }
                                } else {
                                    selectedTagTokens + tag
                                }
                                onTagFilterChange(AnswerTagFilterCodec.encode(updated))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (visibleTags.size % 2 == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            } else if (showDetailedHelp) {
                SettingsHelpText(stringResource(R.string.fill_answer_tag_filter_placeholder), fontSize)
            }
            if (showDetailedHelp) {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsHelpText(stringResource(R.string.fill_answer_tag_filter_help_text), fontSize)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = tokens.textSecondary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = tokens.brandBlue,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = stringResource(
                        R.string.fill_question_filter_summary,
                        fillQuestionFilterSummary.dynamicQuestionCount,
                        fillQuestionFilterSummary.eligibleQuestionCount,
                        fillQuestionFilterSummary.filteredQuestionCount
                    ),
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.brandBlue,
                )
            }
        }
    }
}

/** 等宽标签瓦片：未选浅底，选中蓝底 + 前置对勾。 */
@Composable
private fun SettingsTagTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) tokens.brandBlueSoft else tokens.cardWhite,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                tokens.brandBlue.copy(alpha = 0.55f)
            } else {
                tokens.textSecondary.copy(alpha = 0.22f)
            },
        ),
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = if (selected) 4.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = tokens.brandBlue,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) tokens.brandBlue else tokens.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun String.trimLabelColon(): String = trimEnd('：', ':')
