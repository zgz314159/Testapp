package com.example.testapp.presentation.screen.questionbank

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.model.Question
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.uicommon.design.AppLoadingIndicator

internal const val QUESTION_BANK_SEARCH_BAR_HEIGHT_DP = 48
internal const val MAX_SEARCH_OPTION_PREVIEW_COUNT = 4

internal fun optionLabel(index: Int): String = ('A'.code + index).toChar().toString()

internal fun String.firstDisplayLine(index: Int): String {
    val firstLine = lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    return if (firstLine.isBlank()) "第${index + 1}题" else firstLine
}

@Composable
internal fun QuestionBankSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(HomeDesignTokens.buttonRadius)
    Surface(
        modifier = modifier.height(QUESTION_BANK_SEARCH_BAR_HEIGHT_DP.dp),
        shape = shape,
        color = HomeDesignTokens.surfaceLight,
        shadowElevation = HomeDesignTokens.elevationMedium,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = HomeDesignTokens.primaryContainer,
                tonalElevation = 1.dp,
                shadowElevation = HomeDesignTokens.elevationLow,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = HomeDesignTokens.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = HomeDesignTokens.textPrimaryLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(HomeDesignTokens.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "搜索题库和题目",
                                fontSize = 14.sp,
                                color = HomeDesignTokens.textTertiaryLight,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (value.isNotEmpty()) {
                Surface(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = HomeDesignTokens.surfaceVariantLight,
                    tonalElevation = 1.dp,
                    shadowElevation = 3.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "清空搜索",
                            modifier = Modifier.size(16.dp),
                            tint = HomeDesignTokens.textSecondaryLight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerTreeRow(
    title: androidx.compose.ui.text.AnnotatedString,
    subtitle: String,
    isExpanded: Boolean,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingLoading: Boolean = false,
    accentContainer: Color = HomeDesignTokens.primaryContainer,
    accentIcon: Color = HomeDesignTokens.primary,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = HomeDesignTokens.surfaceLight,
        shadowElevation = HomeDesignTokens.elevationHigh,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = accentContainer,
                tonalElevation = 2.dp,
                shadowElevation = HomeDesignTokens.elevationMedium,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (trailingLoading) {
                        AppLoadingIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = accentIcon,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeDesignTokens.textPrimaryLight,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = HomeDesignTokens.textSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = HomeDesignTokens.primaryContainer,
                tonalElevation = 1.dp,
                shadowElevation = 3.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = HomeDesignTokens.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuestionBankFolderRow(
    folderName: String,
    query: String,
    isExpanded: Boolean,
    itemCount: Int,
    onClick: () -> Unit,
) {
    DrawerTreeRow(
        title = rememberHighlightedQuestionBankText(folderName, query),
        subtitle = "$itemCount 个文件",
        isExpanded = isExpanded,
        trailingIcon = Icons.Filled.Folder,
        onClick = onClick,
        accentContainer = Color(0xFFFFF0D9),
        accentIcon = Color(0xFFE8A838),
    )
}

@Composable
internal fun QuestionBankFileRow(
    fileName: String,
    query: String,
    isExpanded: Boolean,
    isLoading: Boolean,
    matchCount: Int?,
    totalCount: Int?,
    onClick: () -> Unit,
    indent: Int = 0,
) {
    val stats = buildString {
        append("总题数 ${totalCount ?: 0}")
        if (query.isNotBlank()) append(" · 命中 ${matchCount ?: 0}")
    }
    DrawerTreeRow(
        title = rememberHighlightedQuestionBankText(fileName, query),
        subtitle = stats,
        isExpanded = isExpanded,
        trailingIcon = Icons.Filled.Description,
        onClick = onClick,
        trailingLoading = isLoading,
        modifier = Modifier.padding(start = indent.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun QuestionBankSearchQuestionRow(
    question: Question,
    query: String,
    indent: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent.dp, top = 3.dp, bottom = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        color = HomeDesignTokens.surfaceLight,
        tonalElevation = 1.dp,
        shadowElevation = HomeDesignTokens.elevationMedium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "题目",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDesignTokens.primary,
            )
            Text(
                text = rememberHighlightedQuestionBankText(question.content, query),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = HomeDesignTokens.textPrimaryLight,
            )
            if (question.options.isNotEmpty()) {
                Text(
                    text = "选项",
                    fontSize = 10.sp,
                    color = HomeDesignTokens.textSecondaryLight,
                )
                question.options.take(MAX_SEARCH_OPTION_PREVIEW_COUNT).forEachIndexed { index, option ->
                    Text(
                        text = rememberHighlightedQuestionBankText("${optionLabel(index)}. $option", query),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = HomeDesignTokens.textSecondaryLight,
                    )
                }
                if (question.options.size > MAX_SEARCH_OPTION_PREVIEW_COUNT) {
                    Text(
                        text = "还有 ${question.options.size - MAX_SEARCH_OPTION_PREVIEW_COUNT} 个选项",
                        fontSize = 10.sp,
                        color = HomeDesignTokens.textTertiaryLight,
                    )
                }
            }
            Text(
                text = "答案",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDesignTokens.primary,
            )
            Text(
                text = rememberHighlightedQuestionBankText(question.answer, query),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = HomeDesignTokens.textPrimaryLight,
            )
        }
    }
}

internal fun formatQuestionBankRowIndex(questionIndex: Int): String = "第${questionIndex + 1}题"

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun QuestionBankQuestionRow(
    question: Question,
    text: String,
    questionIndex: Int,
    query: String,
    indent: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent.dp, top = 3.dp, bottom = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        color = HomeDesignTokens.surfaceLight,
        tonalElevation = 1.dp,
        shadowElevation = HomeDesignTokens.elevationMedium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = HomeDesignTokens.primaryContainer,
                tonalElevation = 1.dp,
                shadowElevation = HomeDesignTokens.elevationLow,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${questionIndex + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeDesignTokens.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rememberHighlightedQuestionBankText(text, query),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeDesignTokens.textPrimaryLight,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatQuestionBankRowIndex(questionIndex),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeDesignTokens.textSecondaryLight,
                )
            }
        }
    }
}
