package com.example.testapp.presentation.screen.magnetic

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.testapp.presentation.session.magnetic.MagneticToken
import com.example.testapp.presentation.session.magnetic.evaluateMagneticAdjacency
import com.example.testapp.uicommon.design.AppSpacing
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MagneticReorderableTokenFlow(
    tokens: List<MagneticToken>,
    hintedTokenId: Int?,
    scrollState: ScrollState,
    onReturn: (Int) -> Unit,
    onMove: (tokenId: Int, targetIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val boundsById = remember { mutableStateMapOf<Int, MagneticDragBounds>() }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val density = LocalDensity.current
    val connectedTokenIds = remember(tokens) { evaluateMagneticAdjacency(tokens).connectedTokenIds }
    var containerBounds by remember { mutableStateOf<MagneticDragBounds?>(null) }
    var draggedTokenId by remember { mutableStateOf<Int?>(null) }
    var pointerInWindow by remember { mutableStateOf(Offset.Zero) }
    var grabOffset by remember { mutableStateOf(Offset.Zero) }
    var targetIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(tokens.map(MagneticToken::id)) {
        val validIds = tokens.map(MagneticToken::id).toSet()
        boundsById.keys.retainAll(validIds)
        if (draggedTokenId !in validIds) draggedTokenId = null
    }
    LaunchedEffect(draggedTokenId) {
        while (draggedTokenId != null) {
            val edgePx = with(density) { AUTO_SCROLL_EDGE.toPx() }
            val stepPx = with(density) { AUTO_SCROLL_STEP.toPx() }
            val scrollDelta =
                when {
                    pointerInWindow.y < edgePx -> -stepPx
                    pointerInWindow.y > view.height - edgePx -> stepPx
                    else -> 0f
                }
            if (scrollDelta != 0f) {
                scrollState.scrollBy(scrollDelta)
                draggedTokenId?.let { activeId ->
                    targetIndex =
                        calculateTargetIndex(
                            tokens = tokens,
                            activeId = activeId,
                            boundsById = boundsById,
                            pointerInWindow = pointerInWindow,
                            containerBounds = containerBounds,
                        )
                }
            }
            delay(AUTO_SCROLL_FRAME_MS)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    containerBounds = coordinates.boundsInWindow().toDragBounds()
                },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            tokens.forEachIndexed { index, token ->
                key(token.id) {
                    MagneticPlacedToken(
                        token = token,
                        index = index,
                        total = tokens.size,
                        connected = token.id in connectedTokenIds,
                        hinted = hintedTokenId == token.id,
                        dragging = draggedTokenId == token.id,
                        onBoundsChanged = { rect -> boundsById[token.id] = rect.toDragBounds() },
                        onReturn = { onReturn(token.id) },
                        onMove = { destination -> onMove(token.id, destination) },
                        onDragStart = { localOffset ->
                            boundsById[token.id]?.let { bounds ->
                                draggedTokenId = token.id
                                grabOffset = localOffset
                                pointerInWindow = Offset(bounds.left + localOffset.x, bounds.top + localOffset.y)
                                targetIndex = index
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        onDrag = { delta ->
                            pointerInWindow += delta
                            val nextTarget =
                                calculateTargetIndex(
                                    tokens = tokens,
                                    activeId = token.id,
                                    boundsById = boundsById,
                                    pointerInWindow = pointerInWindow,
                                    containerBounds = containerBounds,
                                )
                            if (nextTarget != targetIndex) {
                                targetIndex = nextTarget
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onDragEnd = {
                            if (targetIndex != index) onMove(token.id, targetIndex)
                            draggedTokenId = null
                        },
                        onDragCancel = { draggedTokenId = null },
                    )
                }
            }
        }

        val draggedToken = draggedTokenId?.let { id -> tokens.firstOrNull { it.id == id } }
        val activeContainer = containerBounds
        val draggedBounds = draggedToken?.let { token -> boundsById[token.id] }
        if (draggedToken != null && activeContainer != null && draggedBounds != null) {
            FloatingDraggedToken(
                token = draggedToken,
                hinted = hintedTokenId == draggedToken.id,
                pointerInWindow = pointerInWindow,
                grabOffset = grabOffset,
                draggedBounds = draggedBounds,
                containerBounds = activeContainer,
            )
            InsertionIndicator(
                tokens = tokens,
                draggedTokenId = draggedToken.id,
                targetIndex = targetIndex,
                boundsById = boundsById,
                containerBounds = activeContainer,
            )
        }
    }
}

private fun calculateTargetIndex(
    tokens: List<MagneticToken>,
    activeId: Int,
    boundsById: Map<Int, MagneticDragBounds>,
    pointerInWindow: Offset,
    containerBounds: MagneticDragBounds?,
): Int {
    val boundedPointer =
        containerBounds?.let { bounds ->
            clampMagneticPointer(pointerInWindow.x, pointerInWindow.y, bounds)
        } ?: MagneticDragPoint(pointerInWindow.x, pointerInWindow.y)
    return calculateMagneticInsertionIndex(
        orderedTokenIds = tokens.map(MagneticToken::id),
        draggedTokenId = activeId,
        boundsById = boundsById,
        pointerX = boundedPointer.x,
        pointerY = boundedPointer.y,
    )
}

@Composable
private fun FloatingDraggedToken(
    token: MagneticToken,
    hinted: Boolean,
    pointerInWindow: Offset,
    grabOffset: Offset,
    draggedBounds: MagneticDragBounds,
    containerBounds: MagneticDragBounds,
) {
    val topLeft =
        calculateMagneticFloatingTopLeft(
            containerBounds = containerBounds,
            draggedWidth = draggedBounds.width,
            draggedHeight = draggedBounds.height,
            rawLeft = pointerInWindow.x - grabOffset.x,
            rawTop = pointerInWindow.y - grabOffset.y,
        )
    Box(
        modifier =
            Modifier
                .offset {
                    IntOffset(
                        x = (topLeft.x - containerBounds.left).roundToInt(),
                        y = (topLeft.y - containerBounds.top).roundToInt(),
                    )
                }.zIndex(FLOATING_TOKEN_Z_INDEX),
    ) {
        MagneticFloatingToken(token = token, hinted = hinted)
    }
}

@Composable
private fun InsertionIndicator(
    tokens: List<MagneticToken>,
    draggedTokenId: Int,
    targetIndex: Int,
    boundsById: Map<Int, MagneticDragBounds>,
    containerBounds: MagneticDragBounds,
) {
    val remainingIds = tokens.map(MagneticToken::id).filterNot { it == draggedTokenId }
    val beforeId = remainingIds.getOrNull(targetIndex)
    val anchor = beforeId?.let(boundsById::get) ?: remainingIds.lastOrNull()?.let(boundsById::get) ?: return
    val density = LocalDensity.current
    val indicatorWidthPx = with(density) { INDICATOR_WIDTH.toPx() }
    val indicatorHeightPx = with(density) { INDICATOR_HEIGHT.toPx() }
    val gapPx = with(density) { INDICATOR_GAP.toPx() }
    val topLeft =
        calculateMagneticIndicatorTopLeft(
            anchorBounds = anchor,
            insertBeforeAnchor = beforeId != null,
            containerBounds = containerBounds,
            indicatorWidth = indicatorWidthPx,
            indicatorHeight = indicatorHeightPx,
            gap = gapPx,
        )
    Box(
        modifier =
            Modifier
                .offset {
                    IntOffset(
                        x = (topLeft.x - containerBounds.left).roundToInt(),
                        y = (topLeft.y - containerBounds.top).roundToInt(),
                    )
                }.width(INDICATOR_WIDTH)
                .height(INDICATOR_HEIGHT)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                .zIndex(INDICATOR_Z_INDEX),
    )
}

private fun Rect.toDragBounds(): MagneticDragBounds =
    MagneticDragBounds(left = left, top = top, right = right, bottom = bottom)

private val AUTO_SCROLL_EDGE = 88.dp
private val AUTO_SCROLL_STEP = 11.dp
private val INDICATOR_WIDTH = 2.dp
private val INDICATOR_HEIGHT = 26.dp
private val INDICATOR_GAP = 3.dp
private const val AUTO_SCROLL_FRAME_MS = 16L
private const val FLOATING_TOKEN_Z_INDEX = 2f
private const val INDICATOR_Z_INDEX = 3f
