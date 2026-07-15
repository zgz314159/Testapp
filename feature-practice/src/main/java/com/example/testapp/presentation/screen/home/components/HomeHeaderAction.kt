package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.feature.practice.R

@Composable
fun HomeHeaderAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showBadge: Boolean = false,
    backgroundColor: Color = Color.White,
    iconTint: Color = Color(0xFF5F6B7A),
) {
    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 2.dp else 0.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription,
                    modifier = Modifier.size(21.dp),
                    tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f),
                )
            }
        }

        // 通知红点：仅此元素使用偏移
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(10.dp)
                    .background(Color(0xFFE87461), CircleShape),
            )
        }
    }
}

@Composable
fun HomeSearchAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    HomeHeaderAction(
        icon = Icons.Filled.Search,
        contentDescription = stringResource(R.string.home_search_button),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun HomeNotificationAction(modifier: Modifier = Modifier) {
    HomeHeaderAction(
        icon = Icons.Filled.Notifications,
        contentDescription = stringResource(R.string.home_notification_placeholder),
        onClick = {},
        enabled = false,
        showBadge = true,
        modifier = modifier,
        iconTint = Color(0xFF5F6B7A).copy(alpha = 0.5f),
    )
}
