package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.stepper.CapsuleStepperInput

@Composable
fun SettingsStepperRow(
    label: @Composable () -> Unit,
    contentDescription: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    formatDisplay: (Int) -> String = Int::toString,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 0.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            label()
        }
        CapsuleStepperInput(
            value = value,
            onValueChange = onValueChange,
            minValue = minValue,
            maxValue = maxValue,
            formatDisplay = formatDisplay,
            contentDescription = contentDescription,
            width = 124,
        )
    }
}
