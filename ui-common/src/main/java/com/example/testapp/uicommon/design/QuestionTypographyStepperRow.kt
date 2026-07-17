package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.stepper.CapsuleStepperInput

@Composable
fun QuestionTypographyStepperRow(
    label: String,
    step: Int,
    onStepChange: (Int) -> Unit,
    minStep: Int,
    maxStep: Int,
    formatDisplay: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = tokens.cardWhite,
        tonalElevation = 1.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = tokens.textPrimary,
            )
            CapsuleStepperInput(
                value = step,
                onValueChange = onStepChange,
                minValue = minStep,
                maxValue = maxStep,
                formatDisplay = formatDisplay,
                contentDescription = label,
            )
        }
    }
}
