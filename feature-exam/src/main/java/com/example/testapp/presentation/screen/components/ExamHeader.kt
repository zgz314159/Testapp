package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

@Composable
fun ExamHeader(
    questionType: String,
    currentIndex: Int,
    total: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = questionType,
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "${currentIndex + 1}/$total",
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    LinearProgressIndicator(
        progress = if (total > 0) (currentIndex + 1f) / total else 0f,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

