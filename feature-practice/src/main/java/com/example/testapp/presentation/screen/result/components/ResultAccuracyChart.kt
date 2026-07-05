package com.example.testapp.presentation.screen.result.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun ColumnScope.ResultAccuracyChartSection(
    accuracyList: List<Float>,
    historyCount: Int,
    statColors: ResultStatColors,
    onShowHistory: () -> Unit,
) {
    if (accuracyList.isEmpty()) return

    Spacer(modifier = Modifier.height(AppSpacing.lg))
    Text(
        text = "历史成绩走势",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.align(Alignment.Start).padding(bottom = AppSpacing.xs),
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        val step =
            if (accuracyList.size > 1) {
                size.width / (accuracyList.size - 1).toFloat()
            } else {
                0f
            }
        val points = accuracyList.mapIndexed { idx, v ->
            Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height)
        }
        drawLine(
            statColors.chartAxis,
            Offset(0f, size.height),
            Offset(size.width, size.height),
            strokeWidth = 2f,
        )
        drawLine(statColors.chartAxis, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
        for (i in 0 until points.size - 1) {
            drawLine(primaryColor, points[i], points[i + 1], strokeWidth = 4f)
        }
        points.forEach { drawCircle(secondaryColor, 6f, it) }
    }
    Spacer(modifier = Modifier.height(AppSpacing.sm))
    TextButton(
        onClick = onShowHistory,
        modifier = Modifier.align(Alignment.Start),
    ) {
        Text("查看历史记录 ($historyCount)")
    }
}
