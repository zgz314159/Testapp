package com.example.testapp.presentation.screen.magnetic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.session.magnetic.MagneticRebuildSession
import com.example.testapp.presentation.session.magnetic.MagneticRebuildUiState
import com.example.testapp.uicommon.design.AppCard
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppElevatedConfirmDialog
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.AppThemeColors
import com.example.testapp.uicommon.design.AppTopBarIconButton
import com.example.testapp.uicommon.design.PracticeExamTopBarShell
import com.example.testapp.uicommon.design.QuestionSessionCardContainerLight
import com.example.testapp.uicommon.design.SessionModeBadge
import com.example.testapp.uicommon.design.magneticRebuildModeLabel
import com.example.testapp.uicommon.layout.ScreenSafeScaffold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MagneticRebuildScreen(
    session: MagneticRebuildSession,
    onBack: () -> Unit,
) {
    val state by session.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var lastAdjacency by remember { mutableIntStateOf(0) }
    var showAnswerCard by rememberSaveable { mutableStateOf(false) }
    val tokens = AppElevatedActionSheetTokens

    LaunchedEffect(state.correctAdjacencyCount) {
        if (state.correctAdjacencyCount > lastAdjacency) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        lastAdjacency = state.correctAdjacencyCount
    }
    LaunchedEffect(state.currentCompleted) {
        if (state.currentCompleted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    ScreenSafeScaffold { contentModifier ->
        Column(modifier = contentModifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier.padding(
                        start = AppSpacing.md,
                        end = AppSpacing.md,
                        top = AppSpacing.sm,
                        bottom = AppSpacing.xs,
                    ),
            ) {
                PracticeExamTopBarShell {
                    AppTopBarIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "磁吸重建",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary,
                        )
                        if (!state.isLoading && state.totalClauseCount > 0) {
                            Text(
                                text = "${state.currentClauseIndex + 1}/${state.totalClauseCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.textSecondary,
                            )
                        }
                    }
                    if (!state.isLoading && !state.sessionCompleted && state.totalClauseCount > 0) {
                        AppTopBarIconButton(onClick = { showAnswerCard = true }) {
                            Icon(Icons.Filled.GridView, contentDescription = "答题卡")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
            when {
                state.isLoading -> LoadingContent(Modifier.fillMaxSize())
                state.errorMessage != null ->
                    ErrorContent(
                        message = state.errorMessage.orEmpty(),
                        onBack = onBack,
                        modifier = Modifier.fillMaxSize(),
                    )
                state.sessionCompleted ->
                    SessionCompletedContent(
                        state = state,
                        onBack = onBack,
                        modifier = Modifier.fillMaxSize(),
                    )
                else ->
                    RebuildContent(
                        state = state,
                        dispatch = session::handle,
                        modifier = Modifier.fillMaxSize(),
                    )
            }
        }
    }

    MagneticAnswerCardSheet(
        show = showAnswerCard,
        state = state,
        onDismiss = { showAnswerCard = false },
        onSelect = { index ->
            session.handle(SessionCommand.GoToQuestion(index, source = "magneticAnswerCard"))
            showAnswerCard = false
        },
    )

    if (state.showOriginal) {
        AppElevatedConfirmDialog(
            onDismiss = { session.handle(SessionCommand.MagneticToggleOriginal) },
            title = "完整原文",
            message = state.currentClause?.originalText.orEmpty(),
            confirmLabel = "记住了",
            dismissLabel = "关闭",
            onConfirm = { },
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppElevatedActionSheetTokens.brandBlue)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    Box(
        modifier = modifier.fillMaxSize().padding(AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        AppCard(contentPadding = Modifier.padding(AppSpacing.lg)) {
            Text(
                message,
                color = tokens.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            MagneticPrimaryButton(text = "返回题库", onClick = onBack)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RebuildContent(
    state: MagneticRebuildUiState,
    dispatch: (SessionCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clause = state.currentClause ?: return
    val colors = MaterialTheme.colorScheme
    val tokens = AppElevatedActionSheetTokens
    val success = AppThemeColors.success
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MagneticCompactTaskHeader(state = state)

        MagneticBoardPanel {
            Text("已组装条文", fontWeight = FontWeight.SemiBold, color = tokens.textPrimary)
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            if (state.placed.isEmpty()) {
                Text(
                    "词块将在这里逐步连接",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                    textAlign = TextAlign.Center,
                    color = tokens.textSecondary,
                )
            } else {
                MagneticReorderableTokenFlow(
                    tokens = state.placed,
                    hintedTokenId = state.hintedTokenId,
                    scrollState = scrollState,
                    onReturn = { tokenId ->
                        dispatch(SessionCommand.MagneticReturnToken(tokenId))
                    },
                    onMove = { tokenId, targetIndex ->
                        dispatch(SessionCommand.MagneticMoveToken(tokenId, targetIndex))
                    },
                )
            }
        }

        MagneticBoardPanel {
            Text("待拼词块", fontWeight = FontWeight.SemiBold, color = tokens.textPrimary)
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                state.candidates.forEach { token ->
                    MagneticCandidateToken(
                        token = token,
                        hinted = state.hintedTokenId == token.id,
                        onClick = { dispatch(SessionCommand.MagneticAddToken(token.id)) },
                    )
                }
            }
            if (state.candidates.isEmpty()) {
                Text("全部词块已放入，请检查顺序。", color = tokens.textSecondary)
            }
        }

        MagneticBoardPanel {
            Text(
                text = state.feedback,
                color = if (state.currentCompleted) success else tokens.textPrimary,
                fontWeight = if (state.currentCompleted) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            MagneticActionChip(
                text = "撤销",
                icon = Icons.Filled.Undo,
                enabled = state.canUndo,
                onClick = { dispatch(SessionCommand.MagneticUndo) },
                modifier = Modifier.weight(1f),
            )
            MagneticActionChip(
                text = "提示",
                icon = Icons.Filled.Lightbulb,
                enabled = !state.currentCompleted,
                onClick = { dispatch(SessionCommand.MagneticHint) },
                modifier = Modifier.weight(1f),
            )
            MagneticActionChip(
                text = "原文",
                icon = Icons.Filled.Visibility,
                enabled = true,
                onClick = { dispatch(SessionCommand.MagneticToggleOriginal) },
                modifier = Modifier.weight(1f),
            )
            MagneticActionChip(
                text = "重置",
                icon = Icons.Filled.Refresh,
                enabled = !state.currentCompleted,
                onClick = { dispatch(SessionCommand.MagneticReset) },
                modifier = Modifier.weight(1f),
            )
        }

        MagneticPrimaryButton(
            text =
                if (state.currentCompleted) {
                    if (state.completedClauseCount >= state.totalClauseCount) "完成本轮" else "下一未完成"
                } else {
                    "检查条文"
                },
            onClick = {
                dispatch(if (state.currentCompleted) SessionCommand.MagneticNext else SessionCommand.MagneticCheck)
            },
        )
        Text(
            text = "本条共 ${clause.tokens.size} 个语义块；本轮移动 ${state.moveCount} 次，检查错误 ${state.wrongCheckCount} 次。",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
    }
}

@Composable
private fun MagneticActionChip(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = tokens.cardWhite,
        tonalElevation = 2.dp,
        shadowElevation = tokens.iconElevation,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = if (enabled) tokens.brandBlue else tokens.textSecondary,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = text,
                maxLines = 1,
                color = if (enabled) tokens.textPrimary else tokens.textSecondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MagneticPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = tokens.brandBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = tokens.cardElevation,
                pressedElevation = 4.dp,
            ),
        shape = RoundedCornerShape(tokens.cardCorner),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/** 无描边底板：软阴影 + 纯色圆角，避免 ElevatedCard/Surface 裁切形成左侧白框感。 */
@Composable
internal fun MagneticBoardPanel(
    modifier: Modifier = Modifier,
    contentPadding: Modifier = Modifier.padding(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val containerColor =
        if (AppThemeColors.isDark) {
            MaterialTheme.colorScheme.surface
        } else {
            QuestionSessionCardContainerLight
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = shape, clip = false)
                .background(color = containerColor, shape = shape)
                .then(contentPadding),
        content = content,
    )
}

@Composable
private fun SessionCompletedContent(
    state: MagneticRebuildUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    Box(
        modifier = modifier.fillMaxSize().padding(AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        AppCard(contentPadding = Modifier.padding(AppSpacing.lg)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                SessionModeBadge(label = magneticRebuildModeLabel())
                Text(
                    "本轮重建完成",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                )
                Text(
                    "已恢复 ${state.completedClauseCount} 条条文。磁吸重建用于建立整条结构，之后可回到正式练习检验主动回忆。",
                    textAlign = TextAlign.Center,
                    color = tokens.textSecondary,
                )
                MagneticPrimaryButton(text = "返回题库", onClick = onBack)
            }
        }
    }
}
