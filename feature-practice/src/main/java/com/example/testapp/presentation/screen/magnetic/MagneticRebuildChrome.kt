package com.example.testapp.presentation.screen.magnetic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.session.magnetic.MagneticRebuildUiState
import com.example.testapp.uicommon.component.AnswerCardGrid
import com.example.testapp.uicommon.component.AnswerCardItemState
import com.example.testapp.uicommon.component.AnswerCardListDialogShell
import com.example.testapp.uicommon.component.AnswerCardStatus
import com.example.testapp.uicommon.component.answerCardStatusColors
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppThemeColors
import com.example.testapp.uicommon.design.SessionModeBadge
import com.example.testapp.uicommon.design.magneticRebuildModeLabel

@Composable
internal fun MagneticCompactTaskHeader(
    state: MagneticRebuildUiState,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    val success = AppThemeColors.success
    val progress =
        if (state.totalAdjacencyCount <= 0) {
            0f
        } else {
            state.correctAdjacencyCount.toFloat() / state.totalAdjacencyCount.toFloat()
        }
    MagneticBoardPanel(
        modifier = modifier,
        contentPadding = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SessionModeBadge(label = magneticRebuildModeLabel())
                Text(
                    text = "已完成 ${state.completedClauseCount}/${state.totalClauseCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.textSecondary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "磁吸回路",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textPrimary,
                )
                Text(
                    text =
                        if (state.currentCompleted) {
                            "已接通"
                        } else {
                            "${state.correctAdjacencyCount}/${state.totalAdjacencyCount}"
                        },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.currentCompleted) success else tokens.brandBlue,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = if (state.currentCompleted) success else tokens.brandBlue,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
internal fun MagneticAnswerCardSheet(
    show: Boolean,
    state: MagneticRebuildUiState,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    if (!show) return
    val items =
        state.clauses.mapIndexed { index, clause ->
            val status =
                when (clause.sourceQuestionId) {
                    in state.completedQuestionIds -> AnswerCardStatus.CORRECT
                    in state.startedQuestionIds -> AnswerCardStatus.SELECTED
                    else -> AnswerCardStatus.UNANSWERED
                }
            AnswerCardItemState(
                index = index,
                label = "${index + 1}",
                status = status,
                isCurrent = index == state.currentClauseIndex,
            )
        }
    AnswerCardListDialogShell(onDismiss = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "磁吸重建答题卡",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text =
                            "完成 ${state.completedClauseCount}/${state.totalClauseCount} · 已开始 ${state.startedQuestionIds.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭答题卡")
                }
            }
            AnswerCardGrid(
                items = items,
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
            )
            AnswerCardLegend()
            Text(
                text = "可直接跳过当前条文。已开始但未完成的词块布局会自动保存，稍后可继续。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnswerCardLegend() {
    val statusColors = answerCardStatusColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = statusColors.getValue(AnswerCardStatus.UNANSWERED), text = "未开始")
        LegendItem(color = statusColors.getValue(AnswerCardStatus.SELECTED), text = "已开始")
        LegendItem(color = statusColors.getValue(AnswerCardStatus.CORRECT), text = "已完成")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(14.dp),
            shape = RoundedCornerShape(5.dp),
            color = color,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {}
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
