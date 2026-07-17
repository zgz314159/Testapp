package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun QuestionCardHeaderRow(
    questionTypeLabel: String,
    progressLabel: String,
    questionListLabel: String? = null,
    onOpenQuestionList: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = questionTypeLabel,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = progressLabel,
            style = MaterialTheme.typography.titleMedium
        )
        if (questionListLabel != null && onOpenQuestionList != null) {
            IconButton(
                onClick = onOpenQuestionList,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = questionListLabel,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
