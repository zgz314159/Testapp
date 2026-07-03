package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMemoryCardSection(
    fontSize: Float,
    expanded: Boolean,
    memoryEnabled: Boolean,
    batchSize: Int,
    wrongMode: Int,
    poolMode: Int,
    onExpandedChange: (Boolean) -> Unit,
    onMemoryModeChange: (Boolean) -> Unit,
    onBatchSizeChange: (Int) -> Unit,
    onWrongModeChange: (Int) -> Unit,
    onPoolModeChange: (Int) -> Unit
) {
    SettingsExpandableCardSection(
        title = stringResource(R.string.memory_mode_label_short),
        fontSize = fontSize,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        expandDescription = stringResource(R.string.expand_memory),
        collapseDescription = stringResource(R.string.collapse_memory),
        leadingIcon = Icons.Filled.Psychology
    ) {
        SettingsListSwitchRow(
            label = stringResource(R.string.memory_mode_enable_label),
            fontSize = fontSize,
            checked = memoryEnabled,
            onCheckedChange = onMemoryModeChange
        )
        if (!memoryEnabled) return@SettingsExpandableCardSection

        SettingsCardDivider()
        val batchLabel = stringResource(R.string.memory_batch_count_template, batchSize)
        SettingsStepperRow(
            label = {
                SettingsHeadlineText(batchLabel, fontSize)
            },
            contentDescription = batchLabel,
            value = batchSize,
            onValueChange = onBatchSizeChange,
            minValue = 1,
            maxValue = 100
        )
        SettingsCardDivider()
        SettingsSegmentedChoiceRow(
            label = stringResource(R.string.memory_wrong_mode_label_short),
            fontSize = fontSize,
            options = listOf(
                stringResource(R.string.memory_wrong_mode_retry_short),
                stringResource(R.string.memory_wrong_mode_redo_short)
            ),
            selectedIndex = wrongMode.coerceIn(0, 1),
            onSelectedIndexChange = onWrongModeChange
        )
        SettingsCardDivider()
        SettingsSegmentedChoiceRow(
            label = stringResource(R.string.memory_pool_mode_label_short),
            fontSize = fontSize,
            options = listOf(
                stringResource(R.string.memory_pool_mode_in_out_short),
                stringResource(R.string.memory_pool_mode_round_short)
            ),
            selectedIndex = poolMode.coerceIn(0, 1),
            onSelectedIndexChange = onPoolModeChange
        )
        SettingsHelpText(
            text = stringResource(R.string.memory_mode_help_text),
            fontSize = fontSize,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
