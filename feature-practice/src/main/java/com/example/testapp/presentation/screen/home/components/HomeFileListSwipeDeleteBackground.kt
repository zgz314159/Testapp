package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeFileListSwipeDeleteBackground(
    label: String,
    onDelete: () -> Unit,
    closeAction: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(92.dp)
            .fillMaxHeight()
            .padding(end = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        IconButton(
            onClick = {
                closeAction(true)
                onDelete()
            },
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
