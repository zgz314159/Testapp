package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.settings.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAnswerSettingsCard(
    fontSize: Float,
    practiceExpanded: Boolean,
    examExpanded: Boolean,
    randomPractice: Boolean,
    randomExam: Boolean,
    practiceCount: Int,
    examCount: Int,
    correctDelay: Int,
    wrongDelay: Int,
    examDelay: Int,
    memoryExpanded: Boolean,
    memoryEnabled: Boolean,
    memoryBatchSize: Int,
    memoryWrongMode: Int,
    memoryPoolMode: Int,
    onPracticeExpandedChange: (Boolean) -> Unit,
    onExamExpandedChange: (Boolean) -> Unit,
    onMemoryExpandedChange: (Boolean) -> Unit,
    onRandomPracticeChange: (Boolean) -> Unit,
    onRandomExamChange: (Boolean) -> Unit,
    onPracticeCountChange: (Int) -> Unit,
    onExamCountChange: (Int) -> Unit,
    onCorrectDelayChange: (Int) -> Unit,
    onWrongDelayChange: (Int) -> Unit,
    onExamDelayChange: (Int) -> Unit,
    onMemoryModeChange: (Boolean) -> Unit,
    onMemoryBatchSizeChange: (Int) -> Unit,
    onMemoryWrongModeChange: (Int) -> Unit,
    onMemoryPoolModeChange: (Int) -> Unit
) {
    SettingsCardGroup {
        SettingsExpandableCardSection(
            title = stringResource(R.string.practice_label_short),
            fontSize = fontSize,
            expanded = practiceExpanded,
            onExpandedChange = onPracticeExpandedChange,
            expandDescription = stringResource(R.string.expand_practice),
            collapseDescription = stringResource(R.string.collapse_practice),
            leadingIcon = Icons.Filled.Edit
        ) {
            SettingsListSwitchRow(
                label = stringResource(R.string.random_practice_label_short),
                fontSize = fontSize,
                checked = randomPractice,
                onCheckedChange = onRandomPracticeChange
            )
            SettingsCardDivider()
            val practiceCountLabel = resolveOptionalCountStepperDescription(
                count = practiceCount,
                allLabel = stringResource(R.string.practice_count_all_short),
                countedLabel = stringResource(R.string.practice_count_template, practiceCount)
            )
            SettingsStepperRow(
                label = {
                    SettingsHeadlineText(stringResource(R.string.settings_question_count_label), fontSize)
                },
                contentDescription = practiceCountLabel,
                value = practiceCount,
                onValueChange = onPracticeCountChange,
                minValue = 0,
                maxValue = 100,
                formatDisplay = ::formatCountStepperDisplay
            )
            SettingsCardDivider()
            SettingsListSliderRow(
                label = stringResource(R.string.correct_delay_label_short),
                fontSize = fontSize,
                value = correctDelay.toFloat(),
                valueRange = 0f..10f,
                onValueChange = { onCorrectDelayChange(it.roundToInt()) },
                valueLabel = { "${it.roundToInt()}s" },
                showRangeLabels = true,
                rangeMinLabel = "0s",
                rangeMaxLabel = "10s"
            )
            SettingsCardDivider()
            SettingsListSliderRow(
                label = stringResource(R.string.wrong_delay_label_short),
                fontSize = fontSize,
                value = wrongDelay.toFloat(),
                valueRange = 0f..10f,
                onValueChange = { onWrongDelayChange(it.roundToInt()) },
                valueLabel = { "${it.roundToInt()}s" },
                showRangeLabels = true,
                rangeMinLabel = "0s",
                rangeMaxLabel = "10s"
            )
        }
        SettingsCardDivider()
        SettingsExpandableCardSection(
            title = stringResource(R.string.exam_label_short),
            fontSize = fontSize,
            expanded = examExpanded,
            onExpandedChange = onExamExpandedChange,
            expandDescription = stringResource(R.string.expand_exam),
            collapseDescription = stringResource(R.string.collapse_exam),
            leadingIcon = Icons.AutoMirrored.Filled.Assignment
        ) {
            SettingsListSwitchRow(
                label = stringResource(R.string.random_exam_label_short),
                fontSize = fontSize,
                checked = randomExam,
                onCheckedChange = onRandomExamChange
            )
            SettingsCardDivider()
            val examCountLabel = resolveOptionalCountStepperDescription(
                count = examCount,
                allLabel = stringResource(R.string.exam_count_all_short),
                countedLabel = stringResource(R.string.exam_count_template, examCount)
            )
            SettingsStepperRow(
                label = {
                    SettingsHeadlineText(stringResource(R.string.settings_question_count_label), fontSize)
                },
                contentDescription = examCountLabel,
                value = examCount,
                onValueChange = onExamCountChange,
                minValue = 0,
                maxValue = 100,
                formatDisplay = ::formatCountStepperDisplay
            )
            SettingsCardDivider()
            SettingsListSliderRow(
                label = stringResource(R.string.answer_delay_short),
                fontSize = fontSize,
                value = examDelay.toFloat(),
                valueRange = 0f..10f,
                onValueChange = { onExamDelayChange(it.roundToInt()) },
                valueLabel = { "${it.roundToInt()}s" },
                showRangeLabels = true,
                rangeMinLabel = "0s",
                rangeMaxLabel = "10s"
            )
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
            onPoolModeChange = onMemoryPoolModeChange
        )
    }
}
