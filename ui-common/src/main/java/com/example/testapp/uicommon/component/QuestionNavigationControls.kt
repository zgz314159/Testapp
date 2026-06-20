package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuestionNavigationControls(
    visible: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    enabledPrev: Boolean,
    enabledNext: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev, enabled = enabledPrev) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "上一题", modifier = Modifier.size(32.dp))
        }
        IconButton(onClick = onNext, enabled = enabledNext) {
            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "下一题", modifier = Modifier.size(32.dp))
        }
    }
}
