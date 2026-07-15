package com.example.testapp.presentation.screen.result.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.screen.result.ResultDisplayStats

@Composable
fun ResultCurrentScoreCard(
    stats: ResultDisplayStats,
    modifier: Modifier = Modifier,
) {
    ResultSurfaceCard(modifier) {
        Column(Modifier.padding(20.dp)) {
            ResultSectionHeader(stats.currentLabel, "成绩概览")
            Spacer(Modifier.height(18.dp))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 330.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularScoreGauge(
                            correct = stats.currentScore,
                            total = stats.currentTotal,
                            rate = stats.currentRate.toFloat(),
                            rateText = stats.currentRateText,
                            modifier = Modifier.size(142.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = ResultDashboardColors.Trophy,
                                modifier = Modifier.align(Alignment.CenterHorizontally).size(46.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            MetricGrid(
                                listOf(
                                    Metric("答对", stats.currentScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                    Metric("答错", stats.currentWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                    Metric("未答", stats.currentUnanswered.toString(), Icons.AutoMirrored.Filled.Help, MetricTone.Neutral),
                                    Metric("正确率", "${stats.currentRateText}%", Icons.Default.TrackChanges, MetricTone.Primary),
                                ),
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularScoreGauge(
                            stats.currentScore,
                            stats.currentTotal,
                            stats.currentRate.toFloat(),
                            stats.currentRateText,
                            Modifier.size(156.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        MetricGrid(
                            listOf(
                                Metric("答对", stats.currentScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("答错", stats.currentWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric("未答", stats.currentUnanswered.toString(), Icons.AutoMirrored.Filled.Help, MetricTone.Neutral),
                                Metric("正确率", "${stats.currentRateText}%", Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultOverallScoreCard(
    stats: ResultDisplayStats,
    modifier: Modifier = Modifier,
) {
    val attemptsLabel = if (stats.isExamMode) "累计考试" else "累计次数"
    val attempts = if (stats.isExamMode) stats.actualExamCount else stats.sameFileHistory.size
    ResultSurfaceCard(modifier) {
        Column(Modifier.padding(20.dp)) {
            ResultSectionHeader(stats.overallLabel, "整体统计")
            Spacer(Modifier.height(18.dp))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 330.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SemiCircularGauge(
                            correct = stats.overallScore,
                            total = stats.overallAnswered,
                            rateText = stats.overallRateText,
                            modifier = Modifier.width(146.dp).height(126.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        MetricGrid(
                            metrics = listOf(
                                Metric("累计答对", stats.overallScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("累计答错", stats.overallWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric(attemptsLabel, attempts.toString(), Icons.Default.Schedule, MetricTone.Primary),
                                Metric("累计正确率", "${stats.overallRateText}%", Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SemiCircularGauge(
                            stats.overallScore,
                            stats.overallAnswered,
                            stats.overallRateText,
                            Modifier.width(164.dp).height(132.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        MetricGrid(
                            listOf(
                                Metric("累计答对", stats.overallScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("累计答错", stats.overallWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric(attemptsLabel, attempts.toString(), Icons.Default.Schedule, MetricTone.Primary),
                                Metric("累计正确率", "${stats.overallRateText}%", Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularScoreGauge(
    correct: Int,
    total: Int,
    rate: Float,
    rateText: String,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = rate.coerceIn(0f, 1f),
        animationSpec = tween(650),
        label = "currentScoreGauge",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize().padding(8.dp)) {
            val stroke = 15.dp.toPx()
            drawArc(
                ResultDashboardColors.Track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                ResultDashboardColors.Primary,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$correct/$total",
                color = ResultDashboardColors.TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "正确率 $rateText%",
                color = ResultDashboardColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun SemiCircularGauge(
    correct: Int,
    total: Int,
    rateText: String,
    modifier: Modifier = Modifier,
) {
    val target = if (total > 0) correct.toFloat() / total else 0f
    val progress by animateFloatAsState(
        targetValue = target.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "overallScoreGauge",
    )
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.matchParentSize().padding(horizontal = 8.dp, vertical = 4.dp)) {
            val stroke = 15.dp.toPx()
            val diameter = size.width - stroke
            val top = size.height - diameter / 2f - stroke / 2f
            val arcSize = Size(diameter, diameter)
            val arcTopLeft = Offset(stroke / 2f, top)
            drawArc(
                ResultDashboardColors.Track,
                180f,
                180f,
                false,
                arcTopLeft,
                arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                ResultDashboardColors.Primary,
                180f,
                180f * progress,
                false,
                arcTopLeft,
                arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(
            modifier = Modifier.padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "$correct/$total",
                color = ResultDashboardColors.TextPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "累计正确率 $rateText%",
                color = ResultDashboardColors.TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ResultSectionHeader(title: String, tag: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.width(4.dp).height(28.dp).background(
                ResultDashboardColors.Primary,
                RoundedCornerShape(4.dp),
            ),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            color = ResultDashboardColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            tag,
            color = ResultDashboardColors.Primary,
            fontSize = 11.sp,
            modifier = Modifier.background(ResultDashboardColors.PrimaryLight, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
        )
    }
}

private data class Metric(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tone: MetricTone,
)

private enum class MetricTone { Success, Error, Neutral, Primary }

@Composable
private fun MetricGrid(
    metrics: List<Metric>,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMetrics.forEach { metric ->
                    MetricTile(metric, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricTile(metric: Metric, modifier: Modifier = Modifier) {
    val (color, background) = when (metric.tone) {
        MetricTone.Success -> ResultDashboardColors.Success to ResultDashboardColors.SuccessBackground
        MetricTone.Error -> ResultDashboardColors.Error to ResultDashboardColors.ErrorBackground
        MetricTone.Neutral -> ResultDashboardColors.Neutral to ResultDashboardColors.NeutralBackground
        MetricTone.Primary -> ResultDashboardColors.Primary to ResultDashboardColors.BlueBackground
    }
    Row(
        modifier = modifier
            .height(58.dp)
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(metric.icon, null, tint = color, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(metric.label, color = ResultDashboardColors.TextSecondary, fontSize = 10.sp, maxLines = 1)
            Text(metric.value, color = ResultDashboardColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun ResultSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x160B3A82))
            .border(1.dp, ResultDashboardColors.Border, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ResultDashboardColors.Card),
        elevation = CardDefaults.cardElevation(0.dp),
        content = content,
    )
}
