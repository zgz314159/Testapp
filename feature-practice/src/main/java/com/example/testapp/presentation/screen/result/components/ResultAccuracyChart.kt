package com.example.testapp.presentation.screen.result.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultAccuracyChartSection(
    accuracyList: List<Float>,
    historyCount: Int,
    onShowHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ResultSurfaceCard(modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = ResultDashboardColors.Primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.size(9.dp))
                Text(
                    "历史成绩趋势",
                    modifier = Modifier.weight(1f),
                    color = ResultDashboardColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "正确率(%)",
                    color = ResultDashboardColors.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.background(
                        ResultDashboardColors.NeutralBackground,
                        RoundedCornerShape(10.dp),
                    ).padding(horizontal = 9.dp, vertical = 5.dp),
                )
            }

            if (accuracyList.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(154.dp).padding(horizontal = 20.dp)
                        .background(ResultDashboardColors.NeutralBackground, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "再完成一次练习后，将显示成绩趋势",
                        color = ResultDashboardColors.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                AccuracyLineChart(
                    values = accuracyList.takeLast(9),
                    modifier = Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 12.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ResultDashboardColors.PrimaryLight)
                    .clickable(onClick = onShowHistory)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.History, null, tint = ResultDashboardColors.Primary)
                Spacer(Modifier.size(10.dp))
                Text(
                    "查看历史记录 ($historyCount)",
                    modifier = Modifier.weight(1f),
                    color = ResultDashboardColors.Primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ResultDashboardColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun AccuracyLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    val animation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "historyChart",
    )
    Canvas(modifier) {
        val left = 34.dp.toPx()
        val right = 10.dp.toPx()
        val top = 24.dp.toPx()
        val bottom = 30.dp.toPx()
        val chartWidth = size.width - left - right
        val chartHeight = size.height - top - bottom
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ResultDashboardColors.TextTertiary.toArgb()
            textSize = 10.sp.toPx()
            textAlign = Paint.Align.RIGHT
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ResultDashboardColors.TextPrimary.toArgb()
            textSize = 9.sp.toPx()
            textAlign = Paint.Align.CENTER
        }

        listOf(0, 25, 50, 75, 100).forEach { percent ->
            val y = top + chartHeight * (1f - percent / 100f)
            drawLine(
                color = ResultDashboardColors.Border,
                start = Offset(left, y),
                end = Offset(size.width - right, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 6f)),
            )
            drawContext.canvas.nativeCanvas.drawText(
                percent.toString(),
                left - 7.dp.toPx(),
                y + 3.dp.toPx(),
                labelPaint,
            )
        }

        val step = if (values.size > 1) chartWidth / (values.size - 1) else 0f
        val points = values.mapIndexed { index, value ->
            Offset(
                x = left + index * step,
                y = top + chartHeight * (1f - value.coerceIn(0f, 1f) * animation),
            )
        }
        val fillPath = Path().apply {
            moveTo(points.first().x, top + chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, top + chartHeight)
            close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                listOf(ResultDashboardColors.Primary.copy(alpha = 0.18f), Color.Transparent),
                startY = top,
                endY = top + chartHeight,
            ),
        )
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            linePath,
            ResultDashboardColors.Primary,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        points.forEachIndexed { index, point ->
            drawCircle(ResultDashboardColors.Card, 5.dp.toPx(), point)
            drawCircle(ResultDashboardColors.Primary, 3.dp.toPx(), point)
            val percent = (values[index].coerceIn(0f, 1f) * 100).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$percent%",
                point.x,
                point.y - 9.dp.toPx(),
                valuePaint,
            )
            drawContext.canvas.nativeCanvas.drawText(
                "第${index + 1}次",
                point.x,
                size.height - 7.dp.toPx(),
                valuePaint,
            )
        }
    }
}
