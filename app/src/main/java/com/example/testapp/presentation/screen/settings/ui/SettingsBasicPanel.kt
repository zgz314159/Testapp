package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun SettingsBasicPanel(
    fontSize: Float,
    fontStyle: String,
    onFontSizeChange: (Float) -> Unit,
    onFontStyleChange: (String) -> Unit
) {
    // 字体大小
    Text(
        stringResource(R.string.font_size_label, fontSize.toInt()),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp,
            fontFamily = LocalFontFamily.current
        )
    )
    Slider(
        value = fontSize,
        onValueChange = onFontSizeChange,
        valueRange = 14f..32f,
        steps = 3
    )
    Spacer(modifier = Modifier.height(24.dp))

    // 字体样式
    Text(
        stringResource(R.string.font_style_label),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp,
            fontFamily = LocalFontFamily.current
        )
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = fontStyle == "Normal",
            onClick = { onFontStyleChange("Normal") }
        )
        Text(
            stringResource(R.string.font_style_normal),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = FontFamily.Default)
        )
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(
            selected = fontStyle == "Serif",
            onClick = { onFontStyleChange("Serif") }
        )
        Text(
            stringResource(R.string.font_style_serif),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = FontFamily.Serif)
        )
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(
            selected = fontStyle == "Monospace",
            onClick = { onFontStyleChange("Monospace") }
        )
        Text(
            stringResource(R.string.font_style_monospace),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = FontFamily.Monospace)
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

