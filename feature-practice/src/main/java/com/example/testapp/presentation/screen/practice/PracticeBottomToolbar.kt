package com.example.testapp.presentation.screen.practice

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.example.testapp.feature.practice.R

@Composable
fun PracticeBottomToolbar(
    canRemoveFromMemoryPool: Boolean,
    questionCopyText: String,
    onRemoveFromMemoryPool: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

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
                clipboardManager.setText(AnnotatedString(questionCopyText))
                Toast.makeText(context, context.getString(R.string.copy_question_success), Toast.LENGTH_SHORT).show()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.copy_question)
            )
        }
    }
}
