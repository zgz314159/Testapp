package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.settings.R

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
    onPracticeExpandedChange: (Boolean) -> Unit,
    onExamExpandedChange: (Boolean) -> Unit,
    onRandomPracticeChange: (Boolean) -> Unit,
    onRandomExamChange: (Boolean) -> Unit,
    onPracticeCountChange: (Int) -> Unit,
    onExamCountChange: (Int) -> Unit,
    onCorrectDelayChange: (Int) -> Unit,
    onWrongDelayChange: (Int) -> Unit,
    onExamDelayChange: (Int) -> Unit,
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
            val practiceCountLabel = resolveOptionalCountStepperDescription(
                count = practiceCount,
                allLabel = stringResource(R.string.practice_count_all_short),
                countedLabel = stringResource(R.string.practice_count_template, practiceCount)
            )
            SettingsInsetPanel {
                SettingsInsetSwitchRow(
                    label = stringResource(R.string.random_practice_label_short),
                    checked = randomPractice,
                    onCheckedChange = onRandomPracticeChange,
                )
                SettingsInsetStepperRow(
                    label = stringResource(R.string.settings_question_count_label),
                    contentDescription = practiceCountLabel,
                    value = practiceCount,
                    onValueChange = onPracticeCountChange,
                    minValue = 0,
                    maxValue = 100,
                    formatDisplay = ::formatCountStepperDisplay,
                )
                SettingsInsetStepperRow(
                    label = stringResource(R.string.correct_delay_label_short),
                    contentDescription = stringResource(R.string.correct_delay_label_short),
                    value = correctDelay,
                    onValueChange = onCorrectDelayChange,
                    minValue = 0,
                    maxValue = 10,
                    formatDisplay = ::formatSecondsStepperDisplay,
                )
                SettingsInsetStepperRow(
                    label = stringResource(R.string.wrong_delay_label_short),
                    contentDescription = stringResource(R.string.wrong_delay_label_short),
                    value = wrongDelay,
                    onValueChange = onWrongDelayChange,
                    minValue = 0,
                    maxValue = 10,
                    formatDisplay = ::formatSecondsStepperDisplay,
                )
            }
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
            val examCountLabel = resolveOptionalCountStepperDescription(
                count = examCount,
                allLabel = stringResource(R.string.exam_count_all_short),
                countedLabel = stringResource(R.string.exam_count_template, examCount)
            )
            SettingsInsetPanel {
                SettingsInsetSwitchRow(
                    label = stringResource(R.string.random_exam_label_short),
                    checked = randomExam,
                    onCheckedChange = onRandomExamChange,
                )
                SettingsInsetStepperRow(
                    label = stringResource(R.string.settings_question_count_label),
                    contentDescription = examCountLabel,
                    value = examCount,
                    onValueChange = onExamCountChange,
                    minValue = 0,
                    maxValue = 100,
                    formatDisplay = ::formatCountStepperDisplay,
                )
                SettingsInsetStepperRow(
                    label = stringResource(R.string.answer_delay_short),
                    contentDescription = stringResource(R.string.answer_delay_short),
                    value = examDelay,
                    onValueChange = onExamDelayChange,
                    minValue = 0,
                    maxValue = 10,
                    formatDisplay = ::formatSecondsStepperDisplay,
                )
            }
        }
    }
}
