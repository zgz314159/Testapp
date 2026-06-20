package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.R

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
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPrev,
            enabled = enabledPrev,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.uicommon_prev_question),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onNext,
            enabled = enabledNext,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.uicommon_next_question),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
    }
}
