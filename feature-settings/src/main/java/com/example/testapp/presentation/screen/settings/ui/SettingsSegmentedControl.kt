package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

/** 白底浮起分段选择条，选中项蓝底胶囊；用于 2–3 个互斥选项。 */
@Composable
fun SettingsSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = shape,
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .padding(3.dp),
        ) {
            options.forEachIndexed { index, option ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (selected) tokens.brandBlueSoft else Color.Transparent)
                        .clickable { onSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) tokens.brandBlue else tokens.textSecondary,
                    )
                }
            }
        }
    }
}
