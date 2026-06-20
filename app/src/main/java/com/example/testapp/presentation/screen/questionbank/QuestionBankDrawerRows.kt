package com.example.testapp.presentation.screen.questionbank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Question

// ---- Shared constants & helpers ----

internal const val MAX_SEARCH_OPTION_PREVIEW_COUNT = 4

internal fun optionLabel(index: Int): String = ('A'.code + index).toChar().toString()

internal fun String.firstDisplayLine(index: Int): String {
    val firstLine = lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    return if (firstLine.isBlank()) "第${index + 1}题" else firstLine
}

// ---- Search bar ----

@Composable
internal fun QuestionBankSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier.height(44.dp),
        shape = shape,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f), shape)
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "搜索题库和题目",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "清空搜索",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---- Folder row ----

@Composable
internal fun QuestionBankFolderRow(
    folderName: String,
    query: String,
    isExpanded: Boolean,
    itemCount: Int,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.ChevronRight,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(
                text = rememberHighlightedQuestionBankText(folderName, query),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = { Text("$itemCount 个文件") },
        trailingContent = { Icon(Icons.Filled.Folder, contentDescription = null) }
    )
}

// ---- File row ----

@Composable
internal fun QuestionBankFileRow(
    fileName: String,
    query: String,
    isExpanded: Boolean,
    isLoading: Boolean,
    matchCount: Int?,
    totalCount: Int?,
    onClick: () -> Unit,
    indent: Int = 0
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent.dp)
            .clickable(onClick = onClick),
        leadingContent = {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.ChevronRight,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(
                text = rememberHighlightedQuestionBankText(fileName, query),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            QuestionBankFileStats(
                totalCount = totalCount ?: 0,
                matchCount = matchCount,
                showMatchCount = query.isNotBlank()
            )
        },
        trailingContent = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Filled.Description, contentDescription = null)
            }
        }
    )
}

@Composable
private fun QuestionBankFileStats(
    totalCount: Int,
    matchCount: Int?,
    showMatchCount: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "总题数：$totalCount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showMatchCount) {
            Text(
                text = "命中数：${matchCount ?: 0}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---- Question rows ----

@Composable
internal fun QuestionBankSearchQuestionRow(
    question: Question,
    query: String,
    indent: Int,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent.dp)
            .clickable(onClick = onClick),
        headlineContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "题目",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = rememberHighlightedQuestionBankText(question.content, query),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (question.options.isNotEmpty()) {
                    Text(
                        text = "选项",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    question.options.take(MAX_SEARCH_OPTION_PREVIEW_COUNT).forEachIndexed { index, option ->
                        Text(
                            text = rememberHighlightedQuestionBankText("${optionLabel(index)}. $option", query),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (question.options.size > MAX_SEARCH_OPTION_PREVIEW_COUNT) {
                        Text(
                            text = "还有 ${question.options.size - MAX_SEARCH_OPTION_PREVIEW_COUNT} 个选项",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "答案",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = rememberHighlightedQuestionBankText(question.answer, query),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
internal fun QuestionBankQuestionRow(
    question: Question,
    text: String,
    query: String,
    indent: Int,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent.dp)
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = rememberHighlightedQuestionBankText(text, query),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            Text(
                text = "ID ${question.id}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}
