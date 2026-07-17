package com.example.testapp.presentation.screen.result.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeFileTypeVisualPipeline

@Composable
fun ResultTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = ResultDashboardColors.Card,
            tonalElevation = 1.dp,
            shadowElevation = 6.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = ResultDashboardColors.TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "练习结果",
            style = TextStyle(
                color = ResultDashboardColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color(0x2910264A),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 4f,
                ),
            ),
        )
    }
}

@Composable
fun ResultQuestionBankHeader(
    fileName: String,
    modifier: Modifier = Modifier,
) {
    val visual = remember(fileName) {
        HomeFileTypeVisualPipeline.resolve(fileName, FileStatistics())
    }

    Surface(
        modifier = modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(24.dp),
        color = ResultDashboardColors.Card,
        tonalElevation = 2.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = visual.gradientStart.copy(alpha = 0.12f),
                tonalElevation = 1.dp,
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        visual.icon,
                        contentDescription = null,
                        tint = visual.gradientStart,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier.width(14.dp))
            Text(
                text = fileName.ifBlank { "当前题库" },
                modifier = Modifier.weight(1f),
                color = ResultDashboardColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
