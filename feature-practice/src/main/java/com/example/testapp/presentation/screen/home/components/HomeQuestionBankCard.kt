package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeFileTypeVisualPipeline
import com.example.testapp.presentation.screen.home.HomePerformanceLog
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens

@Composable
fun HomeQuestionBankCard(
    displayName: String,
    fileName: String,
    progressPercent: Int,
    questionCount: Int,
    wrongCount: Int,
    favoriteCount: Int,
    statistics: FileStatistics,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(fileName) {
        HomePerformanceLog.cardEntered(fileName)
        onDispose { }
    }
    val visual = remember(fileName, statistics.primaryQuestionType, statistics.questionTypeStats) {
        HomeFileTypeVisualPipeline.resolve(fileName, statistics)
    }
    val iconBrush = remember(visual.gradientStart, visual.gradientEnd) {
        Brush.linearGradient(listOf(visual.gradientStart, visual.gradientEnd))
    }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidthDp < 380
    val cardHeight = if (isCompact) 84.dp else 96.dp
    val hPadding = if (isCompact) 10.dp else 14.dp
    val iconSize = if (isCompact) 44.dp else 52.dp
    val iconGlyph = if (isCompact) 22.dp else 26.dp
    val ctaWidth = if (isCompact) 68.dp else 78.dp
    val ctaFont = if (isCompact) 10.sp else 11.sp
    val cardShape = RoundedCornerShape(20.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = HomeDesignTokens.questionCardElevation,
            pressedElevation = HomeDesignTokens.elevationMedium,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(start = hPadding, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(15.dp))
                    .background(iconBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    visual.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconGlyph),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f).height(cardHeight - 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B2B4E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    CardStatIconValue(Icons.Default.PieChart, "$progressPercent%", Color(0xFF4F8CFF))
                    CardStatIconValue(Icons.Default.Quiz, questionCount.toString(), Color(0xFF4F8CFF))
                    CardStatIconValue(Icons.Default.ErrorOutline, wrongCount.toString(), Color(0xFFE87461))
                    CardStatIconValue(Icons.Default.Bookmarks, favoriteCount.toString(), Color(0xFFE8A838))
                }
                HomeCardProgressBar(
                    progress = progressPercent / 100f,
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                )
            }

            Surface(
                modifier = Modifier
                    .width(ctaWidth)
                    .clickable(onClick = onCtaClick),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF0F6FF),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (progressPercent > 0) "继续学习" else "开始练习",
                        fontSize = ctaFont,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4F8CFF),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCardProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFEEF2F7)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(3.dp)
                .background(Color(0xFF4F8CFF)),
        )
    }
}

@Composable
private fun CardStatIconValue(
    icon: ImageVector,
    value: String,
    iconColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5F6B7A),
            maxLines = 1,
        )
    }
}
