package com.example.testapp.presentation.screen.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.calculateResultHistoryRecordStats
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.design.AppEmptyStateInline
import com.example.testapp.uicommon.design.AppLazyBottomSheet
import com.example.testapp.uicommon.design.AppSpacing
import java.time.format.DateTimeFormatter

private val CardBackground = Color(0xFFF7F9FD)

@Composable
fun ResultHistorySheet(
    visible: Boolean,
    historyList: List<HistoryRecord>,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }

    val heightFraction = remember(historyList.size) {
        when {
            historyList.isEmpty() -> 0.4f
            historyList.size <= 3 -> 0.52f + historyList.size * 0.06f
            historyList.size <= 6 -> 0.58f + historyList.size * 0.04f
            else -> 0.82f
        }.coerceIn(0.4f, 0.92f)
    }

    AppLazyBottomSheet(onDismiss = onDismiss, heightFraction = heightFraction) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.result_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            )

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AppEmptyStateInline(
                        message = stringResource(R.string.no_history),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = AppSpacing.md,
                        vertical = AppSpacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(historyList) { index, record ->
                        HistoryRecordCard(
                            index = index,
                            record = record,
                            timeText = record.time.format(formatter),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(
    index: Int,
    record: HistoryRecord,
    timeText: String,
) {
    val stats = remember(record) { calculateResultHistoryRecordStats(record) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.result_history_nth, index + 1),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.result_correct_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7D899D),
                )
                Text(
                    text = stats.correct.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2DBE78),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.result_wrong_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7D899D),
                )
                Text(
                    text = stats.wrong.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5A4F),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.result_rate_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7D899D),
                )
                Text(
                    text = stats.rateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F66F3),
                )
            }
        }
    }
}
