package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

@Composable
fun PracticeSubmitControls(
    enabled: Boolean,
    onSubmitClick: () -> Unit,
    label: String,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    swapPrimaryAndLeading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (swapPrimaryAndLeading && leadingContent != null) {
            Button(onClick = onSubmitClick, enabled = enabled) {
                Text(text = label, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.padding(start = 4.dp), content = leadingContent)
        } else {
            if (leadingContent != null) {
                Row(content = leadingContent)
                Spacer(modifier = Modifier.weight(1f))
            }
            Button(onClick = onSubmitClick, enabled = enabled) {
                Text(text = label, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            }
        }
    }
}

