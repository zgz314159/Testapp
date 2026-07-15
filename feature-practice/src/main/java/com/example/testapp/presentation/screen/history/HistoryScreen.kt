package com.example.testapp.presentation.screen.history

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppTopBar
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyList by viewModel.historyList.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    Scaffold(
        containerColor = HomeDesignTokens.backgroundLight,
        topBar = { AppTopBar(title = "学习记录") },
    ) { innerPadding ->
        if (historyList.isEmpty()) {
            AppEmptyState(
                message = "暂无练习记录",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = HomeDesignTokens.pageHorizontalPadding,
                    top = HomeDesignTokens.spacingSm,
                    end = HomeDesignTokens.pageHorizontalPadding,
                    bottom = HomeDesignTokens.pageBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(HomeDesignTokens.cardGap),
            ) {
                item { HistoryOverviewCard(totalRecords = historyList.size) }
                itemsIndexed(historyList) { index, record ->
                    HistoryRecordCard(
                        index = index + 1,
                        record = record,
                        timeText = record.time.format(formatter),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryOverviewCard(totalRecords: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HomeDesignTokens.heroCardRadius),
        colors = CardDefaults.cardColors(containerColor = HomeDesignTokens.primaryContainer.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = HomeDesignTokens.insideCardPadding, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = HomeDesignTokens.primary,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.size(HomeDesignTokens.spacingMd))
            Column {
                Text(
                    text = "你的练习足迹",
                    fontSize = HomeDesignTokens.sectionTitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14264A),
                )
                Text(
                    text = "已累计 $totalRecords 次练习记录",
                    fontSize = HomeDesignTokens.subtitleFontSize,
                    color = HomeDesignTokens.textSecondaryLight,
                )
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
    val displayName = record.fileName
        .orEmpty()
        .removePrefix("practice_")
        .removePrefix("exam_")
        .let(HomeDashboardPipeline::cleanupDisplayName)
        .ifBlank { "本次练习" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HomeDesignTokens.surfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeDesignTokens.elevationLow),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6672F5), Color(0xFF7580F8))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF14264A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = timeText,
                    fontSize = HomeDesignTokens.captionFontSize,
                    color = HomeDesignTokens.textSecondaryLight,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.score}/${record.total}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeDesignTokens.primary,
                )
                Text(
                    text = "正确题数",
                    fontSize = 11.sp,
                    color = HomeDesignTokens.textSecondaryLight,
                )
            }
        }
    }
}
