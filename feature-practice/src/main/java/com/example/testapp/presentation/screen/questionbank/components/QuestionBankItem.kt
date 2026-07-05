package com.example.testapp.presentation.screen.questionbank.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Topic
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

private data class QuestionBankVisualStyle(
    val icon: ImageVector,
    val iconContainerColor: Color,
    val iconTintColor: Color
)

@Composable
fun QuestionBankItem(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val visualStyle = resolveQuestionBankVisualStyle(title, icon)
    val containerColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = containerColor)
                .border(1.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(color = visualStyle.iconContainerColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visualStyle.icon,
                    contentDescription = null,
                    tint = visualStyle.iconTintColor
                )
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "共${count}个题库",
                    fontSize = (LocalFontSize.current.value - 4f).coerceAtLeast(12f).sp,
                    fontFamily = LocalFontFamily.current,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DefaultQuestionBankFolderItem(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    QuestionBankItem(
        title = title,
        count = count,
        icon = Icons.Rounded.Topic,
        modifier = modifier,
        isHighlighted = isHighlighted
    )
}

@Composable
private fun resolveQuestionBankVisualStyle(title: String, fallbackIcon: ImageVector): QuestionBankVisualStyle {
    val lowered = title.lowercase()
    val colorScheme = MaterialTheme.colorScheme

    return when {
        title.contains("高级技师") || title.contains("高级工") -> QuestionBankVisualStyle(
            icon = Icons.Rounded.WorkspacePremium,
            iconContainerColor = colorScheme.secondaryContainer,
            iconTintColor = colorScheme.onSecondaryContainer
        )
        title.contains("技师") || title.contains("培训") || title.contains("学习") -> QuestionBankVisualStyle(
            icon = Icons.Rounded.School,
            iconContainerColor = colorScheme.primaryContainer,
            iconTintColor = colorScheme.onPrimaryContainer
        )
        title.contains("供电") || title.contains("电") || lowered.contains("power") -> QuestionBankVisualStyle(
            icon = Icons.Rounded.Bolt,
            iconContainerColor = colorScheme.tertiaryContainer,
            iconTintColor = colorScheme.onTertiaryContainer
        )
        title.contains("集团") || title.contains("资料") || title.contains("文档") -> QuestionBankVisualStyle(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            iconContainerColor = colorScheme.secondaryContainer,
            iconTintColor = colorScheme.onSecondaryContainer
        )
        title.contains("综合") || title.contains("案例") -> QuestionBankVisualStyle(
            icon = Icons.Rounded.Psychology,
            iconContainerColor = colorScheme.tertiaryContainer,
            iconTintColor = colorScheme.onTertiaryContainer
        )
        title.contains("简答") || title.contains("论述") || title.contains("问答") -> QuestionBankVisualStyle(
            icon = Icons.Rounded.AutoStories,
            iconContainerColor = colorScheme.primaryContainer,
            iconTintColor = colorScheme.onPrimaryContainer
        )
        title.contains("题库") || title.contains("题") -> QuestionBankVisualStyle(
            icon = Icons.Rounded.Category,
            iconContainerColor = colorScheme.secondaryContainer,
            iconTintColor = colorScheme.onSecondaryContainer
        )
        else -> {
            val paletteIndex = (title.hashCode() and Int.MAX_VALUE) % 4
            when (paletteIndex) {
                0 -> QuestionBankVisualStyle(
                    icon = Icons.Rounded.Topic,
                    iconContainerColor = colorScheme.primaryContainer,
                    iconTintColor = colorScheme.onPrimaryContainer
                )
                1 -> QuestionBankVisualStyle(
                    icon = Icons.Rounded.Engineering,
                    iconContainerColor = colorScheme.secondaryContainer,
                    iconTintColor = colorScheme.onSecondaryContainer
                )
                2 -> QuestionBankVisualStyle(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    iconContainerColor = colorScheme.tertiaryContainer,
                    iconTintColor = colorScheme.onTertiaryContainer
                )
                else -> QuestionBankVisualStyle(
                    icon = fallbackIcon,
                    iconContainerColor = colorScheme.primaryContainer,
                    iconTintColor = colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

