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
fun SettingsMemoryPanel(
    expanded: Boolean,
    onToggle: () -> Unit,
    fontSize: Float,
    practiceMemoryMode: Boolean,
    practiceMemoryBatchSize: Int,
    practiceMemoryWrongMode: Int,
    practiceMemoryPoolMode: Int,
    onMemoryModeChange: (Boolean) -> Unit,
    onBatchSizeChange: (Int) -> Unit,
    onWrongModeChange: (Int) -> Unit,
    onPoolModeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.memory_mode_label),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null
        )
    }
    if (expanded) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.memory_mode_label),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = practiceMemoryMode, onCheckedChange = onMemoryModeChange)
        }
        if (practiceMemoryMode) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.memory_batch_count_template, practiceMemoryBatchSize),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
            )
            var memorySliderPosition by remember(practiceMemoryBatchSize) { mutableStateOf(practiceMemoryBatchSize.toFloat()) }
            Slider(
                value = memorySliderPosition,
                onValueChange = { memorySliderPosition = it; onBatchSizeChange(it.roundToInt().coerceIn(1, 100)) },
                valueRange = 1f..100f,
                steps = 98
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.memory_wrong_mode_label), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = practiceMemoryWrongMode == 0, onClick = { onWrongModeChange(0) })
                Text(stringResource(R.string.memory_wrong_mode_retry), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = practiceMemoryWrongMode == 1, onClick = { onWrongModeChange(1) })
                Text(stringResource(R.string.memory_wrong_mode_redo), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.memory_pool_mode_label), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = practiceMemoryPoolMode == 0, onClick = { onPoolModeChange(0) })
                Text(stringResource(R.string.memory_pool_mode_in_out), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = practiceMemoryPoolMode == 1, onClick = { onPoolModeChange(1) })
                Text(stringResource(R.string.memory_pool_mode_round), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.memory_mode_help_text),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = (fontSize - 1).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current, color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

