package com.example.testapp.uicommon.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

/** M3 惯例：ExpandMore + 0°/180° 旋转表示折叠/展开（见 SegmentedListItem / accordion 模式）。 */
@Composable
fun AnswerCardExpandIndicator(
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "answerCardExpand"
    )
    Icon(
        imageVector = Icons.Filled.ExpandMore,
        contentDescription = if (expanded) "折叠" else "展开",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.rotate(rotation)
    )
}
