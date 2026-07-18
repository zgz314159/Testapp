package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.stepper.CapsuleStepperInput
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

/** 折叠区展开后的浅色内嵌面板，与外观「文字」区一致。 */
@Composable
fun SettingsInsetPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = AppElevatedActionSheetTokens.sheetBg,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
    }
}

@Composable
fun SettingsInsetLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = AppElevatedActionSheetTokens.textSecondary,
    )
}

@Composable
fun SettingsInsetSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.weight(1f)) { SettingsInsetLabel(label) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsInsetStepperRow(
    label: String,
    contentDescription: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    formatDisplay: (Int) -> String = Int::toString,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.weight(1f)) { SettingsInsetLabel(label) }
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
