package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily
import kotlin.math.roundToInt

@Composable
fun SettingsListSliderRow(
    label: String,
    fontSize: Float,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    leadingIcon: ImageVector? = null,
    valueLabel: (Float) -> String = { "${it.roundToInt()}" },
    showRangeLabels: Boolean = false,
    rangeMinLabel: String = valueRange.start.roundToInt().toString(),
    rangeMaxLabel: String = valueRange.endInclusive.roundToInt().toString()
) {
    ListItem(
        headlineContent = { SettingsHeadlineText(label, fontSize) },
        leadingContent = leadingIcon?.let { icon ->
            { Icon(imageVector = icon, contentDescription = null) }
        },
        trailingContent = {
            Text(
                text = valueLabel(value),
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current
            )
        },
        supportingContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showRangeLabels) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(rangeMinLabel, fontSize = (fontSize - 2).coerceAtLeast(12f).sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(rangeMaxLabel, fontSize = (fontSize - 2).coerceAtLeast(12f).sp)
                    }
                }
            }
        }
    )
}
