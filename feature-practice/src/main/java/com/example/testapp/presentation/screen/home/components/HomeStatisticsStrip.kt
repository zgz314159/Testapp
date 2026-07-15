package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.practice.R

data class StatItemConfig(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val value: Int,
    val label: String,
    val iconColor: Color,
    val onClick: (() -> Unit)? = null,
)

@Composable
fun HomeStatisticsStrip(
    totalQuestions: Int,
    wrongCount: Int,
    favoriteCount: Int,
    completedCount: Int,
    useLightOnDark: Boolean = false,
    onWrongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val labelColor = if (useLightOnDark) Color.White.copy(alpha = 0.7f) else Color(0xFF8899B4)
    val fontScale = LocalDensity.current.fontScale

    val items = listOf(
        StatItemConfig(Icons.Default.Quiz, totalQuestions, stringResource(R.string.home_stat_total_questions), Color(0xFF4F8CFF)),
        StatItemConfig(Icons.Default.ErrorOutline, wrongCount, stringResource(R.string.home_stat_wrong_count), Color(0xFFE87461), onClick = onWrongClick),
        StatItemConfig(Icons.Default.Bookmarks, favoriteCount, stringResource(R.string.home_stat_favorite_count), Color(0xFFE8A838)),
        StatItemConfig(Icons.Default.CheckCircle, completedCount, stringResource(R.string.home_stat_completed), Color(0xFF42B883)),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            StatBlock(
                icon = item.icon,
                value = item.value.toString(),
                label = item.label,
                iconColor = item.iconColor,
                labelColor = labelColor,
                fontScale = fontScale,
                onClick = item.onClick,
            )
        }
    }
}

@Composable
private fun StatBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    labelColor: Color,
    fontScale: Float,
    onClick: (() -> Unit)? = null,
) {
    val m = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = m,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = value,
                fontSize = (14f / fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF14264A),
                maxLines = 1,
            )
        }
        Text(
            text = label,
                fontSize = (9f / fontScale).sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
        )
    }
}
