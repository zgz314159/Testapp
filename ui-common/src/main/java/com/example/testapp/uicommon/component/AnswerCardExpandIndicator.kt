package com.example.testapp.uicommon.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

/** M3 惯例：ExpandMore + 0°/180° 旋转表示折叠/展开（滚动列表内不做动画，避免掉帧）。 */
@Composable
fun AnswerCardExpandIndicator(
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Filled.ExpandMore,
        contentDescription = if (expanded) "折叠" else "展开",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.rotate(if (expanded) 180f else 0f)
    )
}
