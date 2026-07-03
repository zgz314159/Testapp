package com.example.testapp.uicommon.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ArtifactFullscreenShell(
    modifier: Modifier = Modifier,
    topEndActions: @Composable () -> Unit = {},
    bottomActions: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    ScreenSafeScaffold(modifier) { contentModifier ->
        Box(Modifier.fillMaxSize()) {
            content(contentModifier)
            Box(Modifier.align(Alignment.TopEnd)) {
                topEndActions()
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                bottomActions()
            }
        }
    }
}
