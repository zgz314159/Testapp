package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    val tokens = AppElevatedActionSheetTokens
    Text(
        text = title,
        modifier = modifier.padding(start = 24.dp, top = 24.dp, bottom = 6.dp),
        style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.brandBlue,
            shadow = Shadow(
                color = Color(0x334F8CFF),
                offset = Offset(0f, 1.5f),
                blurRadius = 4f,
            ),
        ),
    )
}
