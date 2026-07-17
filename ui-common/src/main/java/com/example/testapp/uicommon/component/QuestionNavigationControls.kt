package com.example.testapp.uicommon.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.QuestionSessionBottomNavMetrics
import com.example.testapp.uicommon.design.questionSessionSoftCard
import com.example.testapp.uicommon.design.questionSessionSubmitTrayColor

@Composable
fun QuestionNavigationControls(
    visible: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    enabledPrev: Boolean,
    enabledNext: Boolean,
    modifier: Modifier = Modifier,
    onSubmit: (() -> Unit)? = null,
    submitContentDescription: String = "交卷",
    enabledSubmit: Boolean = true,
    onPrevDoubleClick: (() -> Unit)? = null,
    onNextDoubleClick: (() -> Unit)? = null,
    onSubmitDoubleClick: (() -> Unit)? = null,
) {
    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(QuestionSessionBottomNavMetrics.barHeight)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .questionSessionSoftCard(
                    shape = RoundedCornerShape(28.dp),
                    elevation = 12.dp,
                )
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationArrowButton(
                icon = Icons.Filled.ArrowBack,
                contentDescription = "上一题",
                enabled = enabledPrev,
                onClick = onPrev,
                onDoubleClick = onPrevDoubleClick,
            )
            NavigationArrowButton(
                icon = Icons.Filled.ArrowForward,
                contentDescription = "下一题",
                enabled = enabledNext,
                onClick = onNext,
                onDoubleClick = onNextDoubleClick,
            )
        }
        if (onSubmit != null) {
            SubmitNavigationButton(
                contentDescription = submitContentDescription,
                enabled = enabledSubmit,
                onClick = onSubmit,
                onDoubleClick = onSubmitDoubleClick,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubmitNavigationButton(
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tint = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Box(
        modifier = modifier
            .size(84.dp)
            .questionSessionSoftCard(
                shape = CircleShape,
                elevation = 14.dp,
                containerColor = questionSessionSubmitTrayColor(),
            )
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onDoubleClick = onDoubleClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.AssignmentTurnedIn,
            contentDescription = contentDescription,
            modifier = Modifier.size(42.dp),
            tint = tint,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavigationArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)?,
) {
    val tint = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onDoubleClick = onDoubleClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(34.dp),
            tint = tint,
        )
    }
}
