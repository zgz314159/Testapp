package com.example.testapp.presentation.screen.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.presentation.screen.result.ResultDisplayStats
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun ResultCurrentScoreCard(
    stats: ResultDisplayStats,
    statColors: ResultStatColors,
    modifier: Modifier = Modifier,
) {
    ResultSurfaceCard(modifier = modifier.padding(bottom = HomeDesignTokens.cardGap)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stats.currentLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "${stats.currentScore} / ${stats.currentActualAnswered}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            LinearProgressIndicator(
                progress = { stats.currentRate.toFloat() },
                modifier = Modifier.fillMaxWidth().height(AppSpacing.sm)
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ResultStatBlock("答对", "${stats.currentScore}", statColors.correct)
                ResultStatBlock("答错", "${stats.currentWrong}", statColors.wrong)
                ResultStatBlock("未答", "${stats.currentUnanswered}", statColors.unanswered)
                ResultStatBlock("正确率", "${stats.currentRateText}%")
            }
        }
    }
}

@Composable
fun ResultOverallScoreCard(
    stats: ResultDisplayStats,
    statColors: ResultStatColors,
    modifier: Modifier = Modifier,
) {
    ResultSurfaceCard(modifier = modifier.padding(bottom = HomeDesignTokens.cardGap)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stats.overallLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "${stats.overallScore} / ${stats.displayOverallTotal}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            LinearProgressIndicator(
                progress = {
                    if (stats.displayOverallTotal > 0) {
                        stats.overallScore.toFloat() / stats.displayOverallTotal.toFloat()
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth().height(AppSpacing.sm)
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (stats.isExamMode) {
                    ResultStatBlock("累计答对", "${stats.overallScore}", statColors.correct)
                    ResultStatBlock("累计答错", "${stats.overallWrong}", statColors.wrong)
                    ResultStatBlock("累计考试次数", "${stats.actualExamCount}", MaterialTheme.colorScheme.primary)
                    ResultStatBlock(
                        "平均正确率",
                        "${String.format("%.1f", if (stats.overallAnswered > 0) stats.overallScore.toFloat() / stats.overallAnswered * 100 else 0f)}%"
                    )
                } else {
                    ResultStatBlock("累计答对", "${stats.overallScore}", statColors.correct)
                    ResultStatBlock("累计答错", "${stats.overallWrong}", statColors.wrong)
                    ResultStatBlock("累计次数", "${stats.sameFileHistory.size}", MaterialTheme.colorScheme.primary)
                    ResultStatBlock("累计正确率", "${stats.overallRateText}%")
                }
            }
        }
    }
}

data class ResultStatColors(
    val correct: Color,
    val wrong: Color,
    val unanswered: Color,
    val chartAxis: Color,
)

@Composable
fun ResultSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HomeDesignTokens.questionCardRadius),
        colors = CardDefaults.cardColors(containerColor = HomeDesignTokens.surfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeDesignTokens.elevationLow),
        content = content,
    )
}
