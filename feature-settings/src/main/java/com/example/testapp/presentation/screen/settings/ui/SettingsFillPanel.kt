package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.feature.settings.R

@OptIn(ExperimentalLayoutApi::class)
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
    onModeChange: (FillQuestionGenerationMode) -> Unit,
    onBlankCountChange: (Int) -> Unit,
    onRequireCorrectChange: (Boolean) -> Unit,
    onRandomOrderChange: (Boolean) -> Unit,
    onScoreRangeChange: (Int, Int) -> Unit,
    onTagFilterChange: (String) -> Unit,
    onTagFilterClear: () -> Unit,
    showDetailedHelp: Boolean = false
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SettingsHeadlineText(
            stringResource(R.string.fill_question_generation_mode_label),
            fontSize
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_DESC,
                onClick = { onModeChange(FillQuestionGenerationMode.SCORE_DESC) },
                label = { Text(stringResource(R.string.fill_mode_score_desc)) }
            )
            FilterChip(
                selected = fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_ASC,
                onClick = { onModeChange(FillQuestionGenerationMode.SCORE_ASC) },
                label = { Text(stringResource(R.string.fill_mode_score_asc)) }
            )
            FilterChip(
                selected = fillQuestionGenerationMode == FillQuestionGenerationMode.TAG_RANDOM,
                onClick = { onModeChange(FillQuestionGenerationMode.TAG_RANDOM) },
                label = { Text(stringResource(R.string.fill_mode_tag_random)) }
            )
            FilterChip(
                selected = fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_RANGE_RANDOM,
                onClick = { onModeChange(FillQuestionGenerationMode.SCORE_RANGE_RANDOM) },
                label = { Text(stringResource(R.string.fill_mode_score_range_random)) }
            )
            FilterChip(
                selected = fillQuestionGenerationMode == FillQuestionGenerationMode.FULL_ANSWER,
                onClick = { onModeChange(FillQuestionGenerationMode.FULL_ANSWER) },
                label = { Text(stringResource(R.string.fill_mode_full_answer)) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val blankCountLabel = resolveOptionalCountStepperDescription(
            count = fillBlankCount,
            allLabel = stringResource(R.string.fill_blank_count_all),
            countedLabel = stringResource(R.string.fill_blank_count_template, fillBlankCount)
        )
        SettingsStepperRow(
            label = {
                SettingsHeadlineText(blankCountLabel, fontSize)
            },
            contentDescription = blankCountLabel,
            value = fillBlankCount,
            onValueChange = onBlankCountChange,
            minValue = 0,
            maxValue = 11,
            formatDisplay = ::formatBlankCountDisplay
        )
        if (showDetailedHelp) {
            SettingsHelpText(stringResource(R.string.fill_blank_help_text), fontSize)
        }

        val helpTextRes = when (fillQuestionGenerationMode) {
            FillQuestionGenerationMode.SCORE_DESC -> R.string.fill_mode_score_desc_help_text
            FillQuestionGenerationMode.SCORE_ASC -> R.string.fill_mode_score_asc_help_text
            FillQuestionGenerationMode.TAG_RANDOM -> R.string.fill_mode_tag_random_help_text
            FillQuestionGenerationMode.FULL_ANSWER -> R.string.fill_mode_full_answer_help_text
            else -> R.string.fill_mode_score_range_random_help_text
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (showDetailedHelp) {
            SettingsHelpText(stringResource(helpTextRes), fontSize)
        }

        if (fillQuestionGenerationMode == FillQuestionGenerationMode.FULL_ANSWER) {
            Spacer(modifier = Modifier.height(12.dp))
            SettingsHeadlineText(stringResource(R.string.fill_full_answer_completion_label), fontSize)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !fillFullAnswerRequireCorrect,
                    onClick = { onRequireCorrectChange(false) },
                    label = { Text(stringResource(R.string.fill_full_answer_completion_answered)) }
                )
                FilterChip(
                    selected = fillFullAnswerRequireCorrect,
                    onClick = { onRequireCorrectChange(true) },
                    label = { Text(stringResource(R.string.fill_full_answer_completion_correct)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (showDetailedHelp) {
                SettingsHelpText(stringResource(R.string.fill_full_answer_completion_help_text), fontSize)
                SettingsHelpText(
                    stringResource(R.string.fill_full_answer_exam_completion_note),
                    fontSize,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            SettingsHeadlineText(stringResource(R.string.fill_full_answer_order_label), fontSize)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = fillFullAnswerRandomOrder,
                    onClick = { onRandomOrderChange(true) },
                    label = { Text(stringResource(R.string.fill_full_answer_order_random)) }
                )
                FilterChip(
                    selected = !fillFullAnswerRandomOrder,
                    onClick = { onRandomOrderChange(false) },
                    label = { Text(stringResource(R.string.fill_full_answer_order_sequential)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (showDetailedHelp) {
                SettingsHelpText(stringResource(R.string.fill_full_answer_order_help_text), fontSize)
            }
        }

        if (fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_RANGE_RANDOM ||
            fillQuestionGenerationMode == FillQuestionGenerationMode.FULL_ANSWER
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            val scoreLabel = stringResource(R.string.fill_answer_score_label)
            val scoreStepperDescriptions = resolveScoreRangeStepperDescriptions(
                rangeLabel = scoreLabel,
                minValue = fillAnswerScoreMin,
                maxValue = fillAnswerScoreMax,
                minTemplate = stringResource(R.string.settings_stepper_score_min),
                maxTemplate = stringResource(R.string.settings_stepper_score_max)
            )
            SettingsScoreRangeStepperRow(
                label = {
                    Column {
                        SettingsHeadlineText(scoreLabel, fontSize)
                        Text(
                            stringResource(
                                R.string.fill_answer_score_range_template,
                                fillAnswerScoreMin,
                                fillAnswerScoreMax
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                minValue = fillAnswerScoreMin,
                maxValue = fillAnswerScoreMax,
                onRangeChange = onScoreRangeChange,
                minContentDescription = scoreStepperDescriptions.min,
                maxContentDescription = scoreStepperDescriptions.max
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (showDetailedHelp) {
                SettingsHelpText(stringResource(R.string.fill_answer_score_help_text), fontSize)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SettingsHeadlineText(stringResource(R.string.fill_answer_tag_filter_label), fontSize)
        val selectedTagTokens = remember(fillAnswerTagFilter) {
            fillAnswerTagFilter.split(Regex("[,，、；;\\n\\r]+")).map { it.trim() }.filter { it.isNotBlank() }
        }
        if (availableFillAnswerTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableFillAnswerTags.take(24).forEach { tag ->
                    val selected = tag in selectedTagTokens
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val updated = if (selected) {
                                selectedTagTokens.filterNot { it == tag }
                            } else {
                                (selectedTagTokens + tag).distinct()
                            }
                            onTagFilterChange(updated.joinToString(" "))
                        },
                        label = { Text(tag) }
                    )
                }
                if (selectedTagTokens.isNotEmpty()) {
                    SettingsFillTagClearChip(onClear = onTagFilterClear)
                }
            }
        } else if (showDetailedHelp) {
            SettingsHelpText(stringResource(R.string.fill_answer_tag_filter_placeholder), fontSize)
        }
        if (showDetailedHelp) {
            SettingsHelpText(stringResource(R.string.fill_answer_tag_filter_help_text), fontSize)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(
                R.string.fill_question_filter_summary,
                fillQuestionFilterSummary.dynamicQuestionCount,
                fillQuestionFilterSummary.eligibleQuestionCount,
                fillQuestionFilterSummary.filteredQuestionCount
            ),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
