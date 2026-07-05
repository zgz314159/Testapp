package com.example.testapp.uicommon.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class FileCardPalette(
    val containerColor: Color,
    val borderColor: Color
)

@Composable
fun fileCardPalette(tone: FileCardTone): FileCardPalette {
    val scheme = MaterialTheme.colorScheme
    return when (tone) {
        FileCardTone.Mixed -> FileCardPalette(scheme.surfaceContainerHigh, scheme.outline)
        FileCardTone.Single -> FileCardPalette(scheme.tertiaryContainer, scheme.tertiary)
        FileCardTone.Multi -> FileCardPalette(scheme.primaryContainer, scheme.primary)
        FileCardTone.Judge -> FileCardPalette(scheme.errorContainer, scheme.error)
        FileCardTone.Blank -> FileCardPalette(scheme.secondaryContainer, scheme.secondary)
        FileCardTone.Essay -> FileCardPalette(scheme.tertiaryContainer, scheme.tertiary)
        FileCardTone.Composite -> FileCardPalette(scheme.primaryContainer, scheme.primary)
        FileCardTone.Discourse -> FileCardPalette(scheme.secondaryContainer, scheme.secondary)
        FileCardTone.Placeholder0 -> FileCardPalette(scheme.primaryContainer, scheme.primary)
        FileCardTone.Placeholder1 -> FileCardPalette(scheme.secondaryContainer, scheme.secondary)
        FileCardTone.Placeholder2 -> FileCardPalette(scheme.tertiaryContainer, scheme.tertiary)
        FileCardTone.Placeholder3 -> FileCardPalette(scheme.surfaceContainerHigh, scheme.outline)
        FileCardTone.Placeholder4 -> FileCardPalette(scheme.surfaceContainerHighest, scheme.outlineVariant)
    }
}
