package com.example.testapp.presentation.screen.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.ai.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.layout.ArtifactFullscreenShell
import kotlinx.coroutines.launch

@Composable
fun ExplanationScreen(
    text: String,
    onBack: () -> Unit = {},
) {
    val typography = rememberAiArtifactTypography(AiFontScope.PRACTICE)
    val coroutineScope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    val increaseFontText = stringResource(R.string.increase_font)
    val decreaseFontText = stringResource(R.string.decrease_font)
    val settingsText = stringResource(R.string.settings)

    BackHandler {
        onBack()
    }

    ArtifactFullscreenShell(
        topEndActions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = settingsText)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text(increaseFontText) }, onClick = {
                    val next = (typography.fontSize.size + 2).coerceAtMost(32f)
                    typography.fontSize.setSize(next)
                    coroutineScope.launch { typography.fontSize.persistSize(next) }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(decreaseFontText) }, onClick = {
                    val next = (typography.fontSize.size - 2).coerceAtLeast(14f)
                    typography.fontSize.setSize(next)
                    coroutineScope.launch { typography.fontSize.persistSize(next) }
                    menuExpanded = false
                })
            }
        }
    ) { contentModifier ->
        Column(
            modifier = contentModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = typography.fontSize.size.sp,
                    fontFamily = LocalFontFamily.current,
                    lineHeight = (typography.fontSize.size * typography.lineSpacing).sp,
                    letterSpacing = typography.letterSpacing.sp
                )
            )
        }
    }
}
