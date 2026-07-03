package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.RichText
import com.example.testapp.uicommon.component.StemImagesSection

@Composable
fun StemContent(
    content: String,
    stemImages: List<String>,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    lineSpacingMultiplier: Float = 1.3f,
    letterSpacing: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        RichText(
            text = content,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            fontFamily = fontFamily,
            lineSpacingMultiplier = lineSpacingMultiplier,
            letterSpacing = letterSpacing
        )
        if (stemImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            StemImagesSection(
                imagePaths = stemImages,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
