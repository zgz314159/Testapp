package com.example.testapp.presentation.screen.result.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultBottomActionBar(
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit,
    detailEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ResultDashboardColors.Card)
            .border(
                1.dp,
                ResultDashboardColors.Border,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            )
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ResultDashboardColors.TextTertiary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ResultDashboardColors.TextSecondary),
        ) {
            Text("返回首页", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onViewDetail,
            enabled = detailEnabled,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = ResultDashboardColors.Track,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth().height(52.dp).background(
                    Brush.horizontalGradient(
                        if (detailEnabled) {
                            listOf(Color(0xFF356EF5), Color(0xFF1457E8))
                        } else {
                            listOf(ResultDashboardColors.Track, ResultDashboardColors.Track)
                        },
                    ),
                    RoundedCornerShape(26.dp),
                ),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(
                    "答题详情",
                    color = if (detailEnabled) Color.White else ResultDashboardColors.TextTertiary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
