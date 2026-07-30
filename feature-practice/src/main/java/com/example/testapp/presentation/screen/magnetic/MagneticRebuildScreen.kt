package com.example.testapp.presentation.screen.magnetic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.session.magnetic.MagneticRebuildSession
import com.example.testapp.presentation.session.magnetic.MagneticRebuildUiState
import com.example.testapp.uicommon.design.AppThemeColors
import com.example.testapp.uicommon.design.SessionModeBadge
import com.example.testapp.uicommon.design.magneticRebuildModeLabel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MagneticRebuildScreen(
    session: MagneticRebuildSession,
    onBack: () -> Unit,
) {
    val state by session.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var lastAdjacency by remember { mutableIntStateOf(0) }
    var showAnswerCard by rememberSaveable { mutableStateOf(false) }

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

    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("磁吸重建", fontWeight = FontWeight.SemiBold)
                        if (!state.isLoading && state.totalClauseCount > 0) {
                            Text(
                                text = "${state.currentClauseIndex + 1}/${state.totalClauseCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (!state.isLoading && !state.sessionCompleted && state.totalClauseCount > 0) {
                        IconButton(onClick = { showAnswerCard = true }) {
                            Icon(Icons.Filled.GridView, contentDescription = "答题卡")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor = colors.onBackground,
                    actionIconContentColor = colors.onBackground,
                ),
            )
        },
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingContent(Modifier.padding(paddingValues))
            state.errorMessage != null ->
                ErrorContent(
                    message = state.errorMessage.orEmpty(),
                    onBack = onBack,
                    modifier = Modifier.padding(paddingValues),
                )
            state.sessionCompleted ->
                SessionCompletedContent(
                    state = state,
                    onBack = onBack,
                    modifier = Modifier.padding(paddingValues),
                )
            else ->
                RebuildContent(
                    state = state,
                    dispatch = session::handle,
                    modifier = Modifier.padding(paddingValues),
                )
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
        AlertDialog(
            onDismissRequest = { session.handle(SessionCommand.MagneticToggleOriginal) },
            title = { Text("完整原文") },
            text = { Text(state.currentClause?.originalText.orEmpty()) },
            confirmButton = {
                Button(onClick = { session.handle(SessionCommand.MagneticToggleOriginal) }) {
                    Text("记住了")
                }
            },
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(message, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Button(onClick = onBack) { Text("返回题库") }
            }
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
    val success = AppThemeColors.success
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MagneticCompactTaskHeader(state = state)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("已组装条文", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                if (state.placed.isEmpty()) {
                    Text(
                        "词块将在这里逐步连接",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                        textAlign = TextAlign.Center,
                        color = colors.onSurfaceVariant,
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
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("待拼词块", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    Text("全部词块已放入，请检查顺序。", color = colors.onSurfaceVariant)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (state.currentCompleted) AppThemeColors.successSoft else colors.surface,
            border = BorderStroke(1.dp, if (state.currentCompleted) success else colors.outlineVariant),
        ) {
            Text(
                text = state.feedback,
                modifier = Modifier.padding(14.dp),
                color = if (state.currentCompleted) success else colors.onSurface,
                fontWeight = if (state.currentCompleted) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(
                text = "撤销",
                icon = Icons.Filled.Undo,
                enabled = state.canUndo,
                onClick = { dispatch(SessionCommand.MagneticUndo) },
                modifier = Modifier.weight(1f),
            )
            SmallActionButton(
                text = "提示",
                icon = Icons.Filled.Lightbulb,
                enabled = !state.currentCompleted,
                onClick = { dispatch(SessionCommand.MagneticHint) },
                modifier = Modifier.weight(1f),
            )
            SmallActionButton(
                text = "原文",
                icon = Icons.Filled.Visibility,
                enabled = true,
                onClick = { dispatch(SessionCommand.MagneticToggleOriginal) },
                modifier = Modifier.weight(1f),
            )
            SmallActionButton(
                text = "重置",
                icon = Icons.Filled.Refresh,
                enabled = !state.currentCompleted,
                onClick = { dispatch(SessionCommand.MagneticReset) },
                modifier = Modifier.weight(1f),
            )
        }

        Button(
            onClick = {
                dispatch(if (state.currentCompleted) SessionCommand.MagneticNext else SessionCommand.MagneticCheck)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            shape = RoundedCornerShape(17.dp),
        ) {
            Text(
                if (state.currentCompleted) {
                    if (state.completedClauseCount >= state.totalClauseCount) "完成本轮" else "下一未完成"
                } else {
                    "检查条文"
                },
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "本条共 ${clause.tokens.size} 个语义块；本轮移动 ${state.moveCount} 次，检查错误 ${state.wrongCheckCount} 次。",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = ButtonDefaults.ContentPadding,
        shape = RoundedCornerShape(14.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.size(4.dp))
        Text(text, maxLines = 1)
    }
}

@Composable
private fun SessionCompletedContent(
    state: MagneticRebuildUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SessionModeBadge(label = magneticRebuildModeLabel())
                Text("本轮重建完成", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "已恢复 ${state.completedClauseCount} 条条文。磁吸重建用于建立整条结构，之后可回到正式练习检验主动回忆。",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("返回题库")
                }
            }
        }
    }
}
