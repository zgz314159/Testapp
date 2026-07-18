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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.screen.result.ResultDisplayStats

private const val COMPACT_BREAKPOINT_DP = 330

@Composable
fun ResultCurrentScoreCard(
    stats: ResultDisplayStats,
    modifier: Modifier = Modifier,
) {
    ResultSurfaceCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ResultSectionHeader(stats.currentLabel, "成绩概览")
                Spacer(Modifier.weight(1f))
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ResultDashboardColors.PrimaryLight,
                    tonalElevation = 1.dp,
                    shadowElevation = 5.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ResultDashboardColors.Trophy,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val useVertical = maxWidth < COMPACT_BREAKPOINT_DP.dp
                if (useVertical) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularScoreGauge(
                            stats.currentScore,
                            stats.currentTotal,
                            stats.currentRate.toFloat(),
                            stats.currentRateText,
                            Modifier.size(136.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        MetricGrid(
                            listOf(
                                Metric("答对", stats.currentScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("答错", stats.currentWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric("未答", stats.currentUnanswered.toString(), Icons.AutoMirrored.Filled.Help, MetricTone.Neutral),
                                Metric("正确率", stats.currentRateText, Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularScoreGauge(
                            correct = stats.currentScore,
                            total = stats.currentTotal,
                            rate = stats.currentRate.toFloat(),
                            rateText = stats.currentRateText,
                            modifier = Modifier.size(136.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        MetricGrid(
                            metrics = listOf(
                                Metric("答对", stats.currentScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("答错", stats.currentWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric("未答", stats.currentUnanswered.toString(), Icons.AutoMirrored.Filled.Help, MetricTone.Neutral),
                                Metric("正确率", stats.currentRateText, Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                            modifier = Modifier.weight(1f),
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
    val overallRate =
        if (stats.overallAnswered > 0) {
            stats.overallScore.toFloat() / stats.overallAnswered
        } else {
            0f
        }
    val gaugeDescription =
        "累计正确率${stats.overallRateText}，答对${stats.overallScore}题，共作答${stats.overallAnswered}题"

    ResultSurfaceCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            ResultSectionHeader(stats.overallLabel, "整体统计")
            Spacer(Modifier.height(14.dp))

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val useVertical = maxWidth < COMPACT_BREAKPOINT_DP.dp
                if (useVertical) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularScoreGauge(
                            correct = stats.overallScore,
                            total = stats.overallAnswered,
                            rate = overallRate,
                            rateText = stats.overallRateText,
                            modifier = Modifier.size(136.dp),
                            subtitle = "累计正确率",
                            contentDescription = gaugeDescription,
                        )
                        Spacer(Modifier.height(14.dp))
                        MetricGrid(
                            listOf(
                                Metric("累计答对", stats.overallScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("累计答错", stats.overallWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric(attemptsLabel, attempts.toString(), Icons.Default.Schedule, MetricTone.Primary),
                                Metric("累计正确率", stats.overallRateText, Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularScoreGauge(
                            correct = stats.overallScore,
                            total = stats.overallAnswered,
                            rate = overallRate,
                            rateText = stats.overallRateText,
                            modifier = Modifier.size(136.dp),
                            subtitle = "累计正确率",
                            contentDescription = gaugeDescription,
                        )
                        Spacer(Modifier.width(10.dp))
                        MetricGrid(
                            metrics = listOf(
                                Metric("累计答对", stats.overallScore.toString(), Icons.Default.CheckCircle, MetricTone.Success),
                                Metric("累计答错", stats.overallWrong.toString(), Icons.Default.Error, MetricTone.Error),
                                Metric(attemptsLabel, attempts.toString(), Icons.Default.Schedule, MetricTone.Primary),
                                Metric("累计正确率", stats.overallRateText, Icons.Default.TrackChanges, MetricTone.Primary),
                            ),
                            modifier = Modifier.weight(1f),
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
    subtitle: String? = null,
    contentDescription: String = "",
) {
    val progress by animateFloatAsState(
        targetValue = rate.coerceIn(0f, 1f),
        animationSpec = tween(650),
        label = "currentScoreGauge",
    )
    Box(
        modifier = modifier.then(
            if (contentDescription.isNotEmpty()) {
                Modifier.semantics { this.contentDescription = contentDescription }
            } else {
                Modifier
            },
        ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize().padding(8.dp)) {
            val stroke = 12.dp.toPx()
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
                fontSize = if (subtitle != null) 22.sp else 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                rateText,
                color = if (subtitle != null) {
                    ResultDashboardColors.Primary
                } else {
                    ResultDashboardColors.TextSecondary
                },
                fontSize = 14.sp,
                fontWeight = if (subtitle != null) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = ResultDashboardColors.TextSecondary,
                    fontSize = 10.sp,
                )
            }
        }
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
    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        shape = RoundedCornerShape(14.dp),
        color = background,
        tonalElevation = 1.dp,
        shadowElevation = 5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = Color.White,
                    tonalElevation = 1.dp,
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(metric.icon, null, tint = color, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    metric.label,
                    color = ResultDashboardColors.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                metric.value,
                color = ResultDashboardColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
            fontSize = 20.sp,
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

@Composable
fun ResultSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x220B3A82), spotColor = Color(0x280B3A82))
            .border(1.dp, ResultDashboardColors.Border, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ResultDashboardColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content,
    )
}
