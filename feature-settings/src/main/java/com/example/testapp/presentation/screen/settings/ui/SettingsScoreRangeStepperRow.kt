package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.stepper.CapsuleStepperDefaults
import com.example.testapp.uicommon.component.stepper.CapsuleStepperInput
import com.example.testapp.uicommon.component.stepper.StepperScoreRangePipeline

@Composable
fun SettingsScoreRangeStepperRow(
    label: @Composable () -> Unit,
    minValue: Int,
    maxValue: Int,
    onRangeChange: (Int, Int) -> Unit,
    minContentDescription: String,
    maxContentDescription: String,
    modifier: Modifier = Modifier,
    floor: Int = 1,
    ceiling: Int = 10
) {
    Column(modifier = modifier.fillMaxWidth()) {
        label()
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CapsuleStepperInput(
                value = minValue,
                onValueChange = { newMin ->
                    val (min, max) = StepperScoreRangePipeline.withMin(minValue, maxValue, newMin, floor, ceiling)
                    onRangeChange(min, max)
                },
                minValue = floor,
                maxValue = ceiling,
                width = CapsuleStepperDefaults.WIDTH_DP,
                contentDescription = minContentDescription
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "~",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            CapsuleStepperInput(
                value = maxValue,
                onValueChange = { newMax ->
                    val (min, max) = StepperScoreRangePipeline.withMax(minValue, maxValue, newMax, floor, ceiling)
                    onRangeChange(min, max)
                },
                minValue = minValue,
                maxValue = ceiling,
                width = CapsuleStepperDefaults.WIDTH_DP,
                contentDescription = maxContentDescription
            )
        }
    }
}
