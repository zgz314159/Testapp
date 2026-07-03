package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.component.stepper.CapsuleStepperInput

@Composable
fun QuestionTypographyStepperRow(
    label: String,
    step: Int,
    onStepChange: (Int) -> Unit,
    minStep: Int,
    maxStep: Int,
    formatDisplay: (Int) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        CapsuleStepperInput(
            value = step,
            onValueChange = onStepChange,
            minValue = minStep,
            maxValue = maxStep,
            formatDisplay = formatDisplay,
            contentDescription = label
        )
    }
}
