package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens

/**
 * 首页 Hero 卡片——优先保持 2.1:1，必要时扩展到最小内容高度。
 */
@Composable
fun HomeContinueStudyCard(
    fileName: String,
    displayName: String,
    progressPercent: Int,
    totalQuestions: Int,
    wrongCount: Int,
    favoriteCount: Int,
    completedCount: Int,
    onClick: () -> Unit,
    onWrongBookClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val heroGradient = remember {
        Brush.linearGradient(colors = listOf(Color(0xFFF2F7FF), Color(0xFFE8F1FF)))
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val heroHeight = maxOf(maxWidth / 2.1f, 200.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
                .clip(RoundedCornerShape(HomeDesignTokens.heroCardRadius))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(HomeDesignTokens.heroCardRadius),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(heroGradient),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.62f)
                        .fillMaxSize(),
                ) {
                    HeroTrainIllustration(
                        imageRes = R.drawable.home_hero_train,
                        backgroundColor = Color(0xFFF2F7FF),
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 12.dp, end = 14.dp),
                    ) {
                        Text(
                            text = "继续学习",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5F6B7A),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayName,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14264A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "学习进度 $progressPercent%",
                            fontSize = 12.sp,
                            color = Color(0xFF5F6B7A),
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF4F8CFF),
                            trackColor = Color(0xFFDDEBFF),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 10.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.82f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        HomeStatisticsStrip(
                            totalQuestions = totalQuestions,
                            wrongCount = wrongCount,
                            favoriteCount = favoriteCount,
                            completedCount = completedCount,
                            useLightOnDark = false,
                            onWrongClick = onWrongBookClick,
                        )
                    }
                }
            }
        }
    }
}
