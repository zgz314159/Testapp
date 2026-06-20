package com.example.testapp.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily
import kotlin.math.roundToInt

@Composable
fun SettingsFontSection(
    fontSize: Float,
    fontStyle: String,
    soundEnabled: Boolean,
    darkTheme: Boolean,
    examExpanded: Boolean,
    practiceExpanded: Boolean,
    randomExam: Boolean,
    randomPractice: Boolean,
    examCount: Int,
    practiceCount: Int,
    examDelay: Int,
    correctDelay: Int,
    wrongDelay: Int,
    onFontSizeChange: (Float) -> Unit,
    onFontStyleChange: (String) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onExamExpandedChange: (Boolean) -> Unit,
    onPracticeExpandedChange: (Boolean) -> Unit,
    onRandomExamChange: (Boolean) -> Unit,
    onRandomPracticeChange: (Boolean) -> Unit,
    onExamCountChange: (Int) -> Unit,
    onPracticeCountChange: (Int) -> Unit,
    onExamDelayChange: (Int) -> Unit,
    onCorrectDelayChange: (Int) -> Unit,
    onWrongDelayChange: (Int) -> Unit
) {
    SettingsFontControls(
        fontSize = fontSize,
        fontStyle = fontStyle,
        soundEnabled = soundEnabled,
        darkTheme = darkTheme,
        onFontSizeChange = onFontSizeChange,
        onFontStyleChange = onFontStyleChange,
        onSoundEnabledChange = onSoundEnabledChange,
        onDarkThemeChange = onDarkThemeChange
    )

    SettingsExamControls(
        fontSize = fontSize,
        expanded = examExpanded,
        randomExam = randomExam,
        examCount = examCount,
        examDelay = examDelay,
        onExpandedChange = onExamExpandedChange,
        onRandomExamChange = onRandomExamChange,
        onExamCountChange = onExamCountChange,
        onExamDelayChange = onExamDelayChange
    )

    SettingsPracticeControls(
        fontSize = fontSize,
        expanded = practiceExpanded,
        randomPractice = randomPractice,
        practiceCount = practiceCount,
        correctDelay = correctDelay,
        wrongDelay = wrongDelay,
        onExpandedChange = onPracticeExpandedChange,
        onRandomPracticeChange = onRandomPracticeChange,
        onPracticeCountChange = onPracticeCountChange,
        onCorrectDelayChange = onCorrectDelayChange,
        onWrongDelayChange = onWrongDelayChange
    )
}

@Composable
private fun SettingsFontControls(
    fontSize: Float,
    fontStyle: String,
    soundEnabled: Boolean,
    darkTheme: Boolean,
    onFontSizeChange: (Float) -> Unit,
    onFontStyleChange: (String) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit
) {
    SettingsText(stringResource(R.string.font_size_label, fontSize.toInt()), fontSize)
    Slider(
        value = fontSize,
        onValueChange = onFontSizeChange,
        valueRange = 14f..32f,
        steps = 3
    )
    Spacer(modifier = Modifier.height(24.dp))

    SettingsText(stringResource(R.string.font_style_label), fontSize)
    Row(verticalAlignment = Alignment.CenterVertically) {
        FontStyleOption(
            selected = fontStyle == "Normal",
            label = stringResource(R.string.font_style_normal),
            fontSize = fontSize,
            fontFamily = FontFamily.Default,
            onClick = { onFontStyleChange("Normal") }
        )
        Spacer(modifier = Modifier.width(16.dp))
        FontStyleOption(
            selected = fontStyle == "Serif",
            label = stringResource(R.string.font_style_serif),
            fontSize = fontSize,
            fontFamily = FontFamily.Serif,
            onClick = { onFontStyleChange("Serif") }
        )
        Spacer(modifier = Modifier.width(16.dp))
        FontStyleOption(
            selected = fontStyle == "Monospace",
            label = stringResource(R.string.font_style_monospace),
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            onClick = { onFontStyleChange("Monospace") }
        )
    }
    Spacer(modifier = Modifier.height(24.dp))

    SettingsSwitchRow(
        label = stringResource(R.string.sound_label),
        checked = soundEnabled,
        fontSize = fontSize,
        onCheckedChange = onSoundEnabledChange
    )
    Spacer(modifier = Modifier.height(24.dp))

    SettingsSwitchRow(
        label = stringResource(R.string.dark_mode_label),
        checked = darkTheme,
        fontSize = fontSize,
        onCheckedChange = onDarkThemeChange
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SettingsExamControls(
    fontSize: Float,
    expanded: Boolean,
    randomExam: Boolean,
    examCount: Int,
    examDelay: Int,
    onExpandedChange: (Boolean) -> Unit,
    onRandomExamChange: (Boolean) -> Unit,
    onExamCountChange: (Int) -> Unit,
    onExamDelayChange: (Int) -> Unit
) {
    SettingsExpandableHeader(
        title = stringResource(R.string.exam_label),
        expanded = expanded,
        expandDescription = stringResource(R.string.expand_exam),
        collapseDescription = stringResource(R.string.collapse_exam),
        fontSize = fontSize,
        onExpandedChange = onExpandedChange
    )
    if (!expanded) return

    SettingsSwitchRow(
        label = stringResource(R.string.random_exam_label),
        checked = randomExam,
        fontSize = fontSize,
        onCheckedChange = onRandomExamChange
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsText(
        if (examCount == 0) stringResource(R.string.exam_count_all) else stringResource(R.string.exam_count_template, examCount),
        fontSize
    )
    var sliderPosition by remember(examCount) {
        mutableStateOf(if (examCount == 0) 150f else examCount.toFloat())
    }
    Slider(
        value = sliderPosition,
        onValueChange = {
            sliderPosition = it
            onExamCountChange(if (it <= 100f) it.roundToInt() else 0)
        },
        valueRange = 0f..150f,
        steps = 0
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsText(stringResource(R.string.answer_delay, examDelay), fontSize)
    Slider(
        value = examDelay.toFloat(),
        onValueChange = { onExamDelayChange(it.roundToInt()) },
        valueRange = 0f..10f,
        steps = 5
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SettingsPracticeControls(
    fontSize: Float,
    expanded: Boolean,
    randomPractice: Boolean,
    practiceCount: Int,
    correctDelay: Int,
    wrongDelay: Int,
    onExpandedChange: (Boolean) -> Unit,
    onRandomPracticeChange: (Boolean) -> Unit,
    onPracticeCountChange: (Int) -> Unit,
    onCorrectDelayChange: (Int) -> Unit,
    onWrongDelayChange: (Int) -> Unit
) {
    SettingsExpandableHeader(
        title = stringResource(R.string.practice_label),
        expanded = expanded,
        expandDescription = stringResource(R.string.expand_practice),
        collapseDescription = stringResource(R.string.collapse_practice),
        fontSize = fontSize,
        onExpandedChange = onExpandedChange
    )
    if (!expanded) return

    SettingsSwitchRow(
        label = stringResource(R.string.random_practice_label),
        checked = randomPractice,
        fontSize = fontSize,
        onCheckedChange = onRandomPracticeChange
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsText(
        if (practiceCount == 0) stringResource(R.string.practice_count_all) else stringResource(R.string.practice_count_template, practiceCount),
        fontSize
    )
    var practiceSliderPosition by remember(practiceCount) {
        mutableStateOf(if (practiceCount == 0) 150f else practiceCount.toFloat())
    }
    Slider(
        value = practiceSliderPosition,
        onValueChange = {
            practiceSliderPosition = it
            onPracticeCountChange(if (it <= 100f) it.roundToInt() else 0)
        },
        valueRange = 0f..150f,
        steps = 0
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsText(stringResource(R.string.correct_delay_label, correctDelay), fontSize)
    Slider(
        value = correctDelay.toFloat(),
        onValueChange = { onCorrectDelayChange(it.roundToInt()) },
        valueRange = 0f..10f,
        steps = 5
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsText(stringResource(R.string.wrong_delay_label, wrongDelay), fontSize)
    Slider(
        value = wrongDelay.toFloat(),
        onValueChange = { onWrongDelayChange(it.roundToInt()) },
        valueRange = 0f..10f,
        steps = 5
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SettingsExpandableHeader(
    title: String,
    expanded: Boolean,
    expandDescription: String,
    collapseDescription: String,
    fontSize: Float,
    onExpandedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(!expanded) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsText(title, fontSize)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) collapseDescription else expandDescription
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    fontSize: Float,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingsText(label, fontSize)
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FontStyleOption(
    selected: Boolean,
    label: String,
    fontSize: Float,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    RadioButton(selected = selected, onClick = onClick)
    Text(
        label,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp,
            fontFamily = fontFamily
        )
    )
}

@Composable
private fun SettingsText(text: String, fontSize: Float) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp,
            fontFamily = LocalFontFamily.current
        )
    )
}
