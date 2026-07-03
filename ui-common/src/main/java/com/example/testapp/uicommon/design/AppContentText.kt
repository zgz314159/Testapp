package com.example.testapp.uicommon.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

@Composable
fun AppContentText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
    )
}

@Composable
fun AppContentMediumText(
    text: String,
    modifier: Modifier = Modifier
) {
    AppContentText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}
