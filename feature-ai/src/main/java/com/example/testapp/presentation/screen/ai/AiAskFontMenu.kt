package com.example.testapp.presentation.screen.ai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun AiAskFontMenu(
    screenFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    fontSizeStore: suspend (Float) -> Unit,
    settingsLabel: String,
    increaseFontLabel: String,
    decreaseFontLabel: String
) {
    val coroutineScope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    IconButton(onClick = { menuExpanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = settingsLabel)
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(increaseFontLabel) },
            onClick = {
                val next = (screenFontSize + 2f).coerceAtMost(32f)
                onFontSizeChange(next)
                coroutineScope.launch { fontSizeStore(next) }
                menuExpanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(decreaseFontLabel) },
            onClick = {
                val next = (screenFontSize - 2f).coerceAtLeast(14f)
                onFontSizeChange(next)
                coroutineScope.launch { fontSizeStore(next) }
                menuExpanded = false
            }
        )
    }
}
