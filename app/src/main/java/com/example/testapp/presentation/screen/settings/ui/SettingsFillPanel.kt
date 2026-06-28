package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsFillPanel(
    expanded: Boolean,
    onToggle: () -> Unit,
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
    onTagFilterClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.fill_question_label),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) stringResource(R.string.collapse_fill_settings) else stringResource(R.string.expand_fill_settings)
        )
    }
    if (!expanded) return

    Text(stringResource(R.string.fill_question_generation_mode_label), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_DESC, onClick = { onModeChange(FillQuestionGenerationMode.SCORE_DESC) }, label = { Text(stringResource(R.string.fill_mode_score_desc)) })
        FilterChip(selected = fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_ASC, onClick = { onModeChange(FillQuestionGenerationMode.SCORE_ASC) }, label = { Text(stringResource(R.string.fill_mode_score_asc)) })
        FilterChip(selected = fillQuestionGenerationMode == FillQuestionGenerationMode.TAG_RANDOM, onClick = { onModeChange(FillQuestionGenerationMode.TAG_RANDOM) }, label = { Text(stringResource(R.string.fill_mode_tag_random)) })
        FilterChip(selected = fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_RANGE_RANDOM, onClick = { onModeChange(FillQuestionGenerationMode.SCORE_RANGE_RANDOM) }, label = { Text(stringResource(R.string.fill_mode_score_range_random)) })
        FilterChip(selected = fillQuestionGenerationMode == FillQuestionGenerationMode.FULL_ANSWER, onClick = { onModeChange(FillQuestionGenerationMode.FULL_ANSWER) }, label = { Text(stringResource(R.string.fill_mode_full_answer)) })
    }
    Spacer(modifier = Modifier.height(8.dp))

    Text(
        if (fillBlankCount == 0) stringResource(R.string.fill_blank_count_all) else stringResource(R.string.fill_blank_count_template, fillBlankCount),
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
    )
    var fillBlankSliderPosition by remember(fillBlankCount) { mutableStateOf(if (fillBlankCount == 0) 12f else fillBlankCount.toFloat()) }
    Slider(value = fillBlankSliderPosition, onValueChange = { fillBlankSliderPosition = it; onBlankCountChange(if (it >= 12f) 0 else it.roundToInt().coerceAtLeast(1)) }, valueRange = 1f..12f, steps = 10)
    Text(stringResource(R.string.fill_blank_help_text), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))
    Spacer(modifier = Modifier.height(12.dp))

    val helpTextRes = when (fillQuestionGenerationMode) {
        FillQuestionGenerationMode.SCORE_DESC -> R.string.fill_mode_score_desc_help_text
        FillQuestionGenerationMode.SCORE_ASC -> R.string.fill_mode_score_asc_help_text
        FillQuestionGenerationMode.TAG_RANDOM -> R.string.fill_mode_tag_random_help_text
        FillQuestionGenerationMode.FULL_ANSWER -> R.string.fill_mode_full_answer_help_text
        else -> R.string.fill_mode_score_range_random_help_text
    }
    Text(stringResource(helpTextRes), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))

    if (fillQuestionGenerationMode == FillQuestionGenerationMode.FULL_ANSWER) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.fill_full_answer_completion_label), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !fillFullAnswerRequireCorrect, onClick = { onRequireCorrectChange(false) }, label = { Text(stringResource(R.string.fill_full_answer_completion_answered)) })
            FilterChip(selected = fillFullAnswerRequireCorrect, onClick = { onRequireCorrectChange(true) }, label = { Text(stringResource(R.string.fill_full_answer_completion_correct)) })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.fill_full_answer_completion_help_text), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))
        Text(stringResource(R.string.fill_full_answer_exam_completion_note), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current, color = MaterialTheme.colorScheme.secondary))
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.fill_full_answer_order_label), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = fillFullAnswerRandomOrder, onClick = { onRandomOrderChange(true) }, label = { Text(stringResource(R.string.fill_full_answer_order_random)) })
            FilterChip(selected = !fillFullAnswerRandomOrder, onClick = { onRandomOrderChange(false) }, label = { Text(stringResource(R.string.fill_full_answer_order_sequential)) })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.fill_full_answer_order_help_text), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))
    }

    if (fillQuestionGenerationMode == FillQuestionGenerationMode.SCORE_RANGE_RANDOM || fillQuestionGenerationMode == FillQuestionGenerationMode.FULL_ANSWER) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.fill_answer_score_label), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
        Text(stringResource(R.string.fill_answer_score_range_template, fillAnswerScoreMin, fillAnswerScoreMax), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
        var fillAnswerScoreRange by remember(fillAnswerScoreMin, fillAnswerScoreMax) { mutableStateOf(fillAnswerScoreMin.toFloat()..fillAnswerScoreMax.toFloat()) }
        RangeSlider(value = fillAnswerScoreRange, onValueChange = { fillAnswerScoreRange = it; onScoreRangeChange(it.start.roundToInt().coerceIn(1, 10), it.endInclusive.roundToInt().coerceIn(it.start.roundToInt().coerceIn(1, 10), 10)) }, valueRange = 1f..10f, steps = 8)
        Text(stringResource(R.string.fill_answer_score_help_text), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(12.dp))

    Text(stringResource(R.string.fill_answer_tag_filter_label), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    val selectedTagTokens = remember(fillAnswerTagFilter) { fillAnswerTagFilter.split(Regex("[,，、；;\\n\\r]+")).map { it.trim() }.filter { it.isNotBlank() } }
    if (availableFillAnswerTags.isNotEmpty()) {
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            availableFillAnswerTags.take(24).forEach { tag ->
                val selected = tag in selectedTagTokens
                FilterChip(selected = selected, onClick = { val updated = if (selected) selectedTagTokens.filterNot { it == tag } else (selectedTagTokens + tag).distinct(); onTagFilterChange(updated.joinToString(" ")) }, label = { Text(tag, style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current)) })
            }
            if (selectedTagTokens.isNotEmpty()) {
                AssistChip(onClick = onTagFilterClear, label = { Text(stringResource(R.string.fill_answer_tag_filter_clear), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current)) })
            }
        }
    } else {
        Text(stringResource(R.string.fill_answer_tag_filter_placeholder), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))
    }
    Text(stringResource(R.string.fill_answer_tag_filter_help_text), style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current))
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        stringResource(R.string.fill_question_filter_summary, fillQuestionFilterSummary.dynamicQuestionCount, fillQuestionFilterSummary.eligibleQuestionCount, fillQuestionFilterSummary.filteredQuestionCount),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current, color = MaterialTheme.colorScheme.primary)
    )
    Spacer(modifier = Modifier.height(24.dp))
}


