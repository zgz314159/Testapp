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
import kotlin.math.roundToInt

@Composable
fun SettingsPracticePanel(
    expanded: Boolean,
    onToggle: () -> Unit,
    fontSize: Float,
    randomPractice: Boolean,
    practiceCount: Int,
    correctDelay: Int,
    wrongDelay: Int,
    onRandomChange: (Boolean) -> Unit,
    onCountChange: (Int) -> Unit,
    onCorrectDelayChange: (Int) -> Unit,
    onWrongDelayChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.practice_label),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) stringResource(R.string.collapse_practice) else stringResource(R.string.expand_practice)
        )
    }
    if (expanded) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.random_practice_label), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = randomPractice, onCheckedChange = onRandomChange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (practiceCount == 0) stringResource(R.string.practice_count_all) else stringResource(R.string.practice_count_template, practiceCount),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
        )
        var practiceSliderPosition by remember(practiceCount) { mutableStateOf(if (practiceCount == 0) 150f else practiceCount.toFloat()) }
        Slider(
            value = practiceSliderPosition,
            onValueChange = { practiceSliderPosition = it; onCountChange(if (it <= 100f) it.roundToInt() else 0) },
            valueRange = 0f..150f,
            steps = 0
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.correct_delay_label, correctDelay), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
        Slider(value = correctDelay.toFloat(), onValueChange = { onCorrectDelayChange(it.roundToInt()) }, valueRange = 0f..10f, steps = 5)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.wrong_delay_label, wrongDelay), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
        Slider(value = wrongDelay.toFloat(), onValueChange = { onWrongDelayChange(it.roundToInt()) }, valueRange = 0f..10f, steps = 5)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

