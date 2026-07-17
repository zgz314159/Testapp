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
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.ResultHistoryRecordStats
import com.example.testapp.domain.model.calculateResultHistoryRecordStats

@Composable
fun ResultAccuracyChartSection(
    accuracyList: List<Float>,
    historyCount: Int,
    onShowHistory: () -> Unit,
    historyRecords: List<HistoryRecord> = emptyList(),
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
                    records = historyRecords.takeLast(9),
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 12.dp),
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
    records: List<HistoryRecord>,
    modifier: Modifier = Modifier,
) {
    val animation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "historyChart",
    )

    // 获取记录总数量上下文 - 从 records 知道是第几次
    val totalHistoryCount = records.size

    Canvas(modifier) {
        val left = 42.dp.toPx() // 左侧边距增加到 42dp 避免刻度重叠
        val right = 16.dp.toPx() // 右侧边距至少 16dp
        val top = 24.dp.toPx()
        val bottom = 38.dp.toPx() // 底部留更多空间给横轴标签
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
        val axisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ResultDashboardColors.TextTertiary.toArgb()
            textSize = 9.sp.toPx()
            textAlign = Paint.Align.CENTER
        }

        // 纵轴刻度：0, 25, 50, 75, 100
        val yLabels = listOf(0, 25, 50, 75, 100)
        yLabels.forEach { percent ->
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

        // 没有足够数据点时不绘制折线
        if (values.isEmpty()) return@Canvas

        val step = if (values.size > 1) chartWidth / (values.size - 1) else 0f
        val points = values.mapIndexed { index, value ->
            Offset(
                x = left + index * step,
                y = top + chartHeight * (1f - value.coerceIn(0f, 1f) * animation),
            )
        }

        // 填充区域
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

        // 折线
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            linePath,
            ResultDashboardColors.Primary,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // 数据点标签
        val recordStats: List<ResultHistoryRecordStats> = records.map { calculateResultHistoryRecordStats(it) }
        val firstVisibleIndex = maxOf(0, records.size - values.size)

        points.forEachIndexed { index, point ->
            // 数据点圆
            drawCircle(ResultDashboardColors.Card, 5.dp.toPx(), point)
            drawCircle(ResultDashboardColors.Primary, 3.dp.toPx(), point)

            // 数据值标签（百分比格式，保留精度）
            val stats = recordStats.getOrNull(index)
            val percentLabel = stats?.rateText ?: "${(values[index].coerceIn(0f, 1f) * 100).toInt()}%"

            // 根据位置决定对齐方式
            val previousValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = valuePaint.color
                textSize = valuePaint.textSize
            }
            when (index) {
                0 -> previousValuePaint.textAlign = Paint.Align.LEFT
                values.lastIndex -> previousValuePaint.textAlign = Paint.Align.RIGHT
                else -> previousValuePaint.textAlign = Paint.Align.CENTER
            }

            val labelX = when (index) {
                0 -> point.x + 5.dp.toPx()
                values.lastIndex -> point.x - 5.dp.toPx()
                else -> point.x
            }
            val labelY = (point.y - 10.dp.toPx()).coerceAtLeast(top + 8.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                percentLabel,
                labelX,
                labelY,
                previousValuePaint,
            )

            // 横轴：第N次序号（保留真实序号）
            val nth = firstVisibleIndex + index + 1
            val nthX = when (index) {
                0 -> point.x + 4.dp.toPx()
                values.lastIndex -> point.x - 4.dp.toPx()
                else -> point.x
            }
            drawContext.canvas.nativeCanvas.drawText(
                "第${nth}次",
                nthX,
                size.height - 7.dp.toPx(),
                axisLabelPaint,
            )
        }
    }
}
