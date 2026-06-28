package com.example.testapp.uicommon.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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
    onSubmitDoubleClick: (() -> Unit)? = null
) {
    if (!visible) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationArrowButton(
            icon = Icons.Filled.ArrowBack,
            contentDescription = "上一题",
            enabled = enabledPrev,
            onClick = onPrev,
            onDoubleClick = onPrevDoubleClick
        )
        if (onSubmit != null) {
            SubmitNavigationButton(
                contentDescription = submitContentDescription,
                enabled = enabledSubmit,
                onClick = onSubmit,
                onDoubleClick = onSubmitDoubleClick
            )
        }
        NavigationArrowButton(
            icon = Icons.Filled.ArrowForward,
            contentDescription = "下一题",
            enabled = enabledNext,
            onClick = onNext,
            onDoubleClick = onNextDoubleClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubmitNavigationButton(
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)?
) {
    val tint = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onDoubleClick = onDoubleClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AssignmentTurnedIn,
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp),
            tint = tint
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
    onDoubleClick: (() -> Unit)?
) {
    val tint = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onDoubleClick = onDoubleClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp),
            tint = tint
        )
    }
}
