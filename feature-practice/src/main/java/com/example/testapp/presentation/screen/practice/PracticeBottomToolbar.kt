package com.example.testapp.presentation.screen.practice

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.practice.R
import kotlinx.coroutines.launch

@Composable
fun PracticeBottomToolbar(
    canRemoveFromMemoryPool: Boolean,
    questionCopyText: String,
    onRemoveFromMemoryPool: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val clipboardScope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (canRemoveFromMemoryPool) {
            IconButton(onClick = onRemoveFromMemoryPool) {
                Icon(
                    imageVector = Icons.Outlined.RemoveCircle,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                clipboardScope.launch {
                    clipboard.setClipEntry(
                        ClipEntry(ClipData.newPlainText("question", questionCopyText))
                    )
                    Toast.makeText(context, context.getString(R.string.copy_question_success), Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.copy_question)
            )
        }
    }
}
