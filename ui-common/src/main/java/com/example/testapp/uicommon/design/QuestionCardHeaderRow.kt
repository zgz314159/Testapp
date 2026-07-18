package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    onOpenQuestionList: (() -> Unit)? = null,
    modeLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = questionTypeLabel,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // 中间弹性区：徽标始终居中于「题型」与「进度」之间
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (modeLabel != null) {
                SessionModeBadge(label = modeLabel)
            }
        }
        Text(
            text = progressLabel,
            style = MaterialTheme.typography.bodyMedium
        )
        if (questionListLabel != null && onOpenQuestionList != null) {
            IconButton(
                onClick = onOpenQuestionList,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = questionListLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
