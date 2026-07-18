package com.example.testapp.uicommon.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.testapp.uicommon.R
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens
import com.example.testapp.uicommon.design.AiChatSourceRef
import com.example.testapp.uicommon.design.AppSpacing

/** 回答底部「N 个网页」胶囊，点按展开搜索结果列表。 */
@Composable
fun AiChatSourcesChip(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = AiChatPromptDesignTokens.pageBackground,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AiChatPromptDesignTokens.brandBlue,
            )
            Text(
                text = stringResource(R.string.uicommon_ai_sources_count, count),
                style = MaterialTheme.typography.labelMedium,
                color = AiChatPromptDesignTokens.textSecondary,
            )
        }
    }
}

/** 搜索结果底部弹层：favicon + 站点/时间 + 编号 + 标题 + 摘要，点按跳转浏览器。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatSourcesSheet(
    sources: List<AiChatSourceRef>,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AiChatPromptDesignTokens.cardWhite,
    ) {
        Text(
            text = stringResource(R.string.uicommon_ai_sources_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.sm),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(sources, key = { it.index }) { source ->
                SourceItem(
                    source = source,
                    onClick = { runCatching { uriHandler.openUri(source.url) } },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = AppSpacing.md),
                    color = AiChatPromptDesignTokens.pageBackground,
                )
            }
        }
    }
}

@Composable
private fun SourceItem(
    source: AiChatSourceRef,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SourceFavicon(source)
                Text(
                    text = source.host.ifBlank { source.url },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = AiChatPromptDesignTokens.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (source.publishedDate.isNotBlank()) {
                    Text(
                        text = source.publishedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = AiChatPromptDesignTokens.textSecondary.copy(alpha = 0.7f),
                    )
                }
            }
            SourceIndexBadge(index = source.index)
        }
        Text(
            text = source.title.ifBlank { source.url },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (source.snippet.isNotBlank()) {
            Text(
                text = source.snippet,
                style = MaterialTheme.typography.bodySmall,
                color = AiChatPromptDesignTokens.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SourceFavicon(source: AiChatSourceRef) {
    SubcomposeAsyncImage(
        model = source.faviconUrl,
        contentDescription = null,
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        error = { FaviconFallback() },
        loading = { FaviconFallback() },
    )
}

@Composable
private fun FaviconFallback() {
    Icon(
        imageVector = Icons.Filled.Public,
        contentDescription = null,
        tint = AiChatPromptDesignTokens.textSecondary.copy(alpha = 0.6f),
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun SourceIndexBadge(index: Int) {
    Surface(
        shape = CircleShape,
        color = AiChatPromptDesignTokens.brandBlue.copy(alpha = 0.12f),
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AiChatPromptDesignTokens.brandBlue,
            )
        }
    }
}
