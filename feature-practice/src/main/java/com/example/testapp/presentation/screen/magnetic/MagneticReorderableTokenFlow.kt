package com.example.testapp.presentation.screen.magnetic

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.testapp.presentation.session.magnetic.MagneticToken
import com.example.testapp.presentation.session.magnetic.evaluateMagneticAdjacency
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
                        calculateMagneticInsertionIndex(
                            orderedTokenIds = tokens.map(MagneticToken::id),
                            draggedTokenId = activeId,
                            boundsById = boundsById,
                            pointerX = pointerInWindow.x,
                            pointerY = pointerInWindow.y,
                        )
                }
            }
            delay(AUTO_SCROLL_FRAME_MS)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                calculateMagneticInsertionIndex(
                                    orderedTokenIds = tokens.map(MagneticToken::id),
                                    draggedTokenId = token.id,
                                    boundsById = boundsById,
                                    pointerX = pointerInWindow.x,
                                    pointerY = pointerInWindow.y,
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
    }

    val draggedToken = draggedTokenId?.let { id -> tokens.firstOrNull { it.id == id } }
    if (draggedToken != null) {
        FloatingDraggedToken(
            token = draggedToken,
            hinted = hintedTokenId == draggedToken.id,
            topLeft = pointerInWindow - grabOffset,
        )
        InsertionIndicator(
            tokens = tokens,
            draggedTokenId = draggedToken.id,
            targetIndex = targetIndex,
            boundsById = boundsById,
        )
    }
}

@Composable
private fun FloatingDraggedToken(
    token: MagneticToken,
    hinted: Boolean,
    topLeft: Offset,
) {
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
        properties = PopupProperties(focusable = false, clippingEnabled = false),
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
) {
    val remainingIds = tokens.map(MagneticToken::id).filterNot { it == draggedTokenId }
    val beforeId = remainingIds.getOrNull(targetIndex)
    val anchor = beforeId?.let(boundsById::get) ?: remainingIds.lastOrNull()?.let(boundsById::get) ?: return
    val x = if (beforeId != null) anchor.left - INDICATOR_GAP_PX else anchor.right + INDICATOR_GAP_PX
    val density = LocalDensity.current
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(x.roundToInt(), anchor.top.roundToInt()),
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(with(density) { anchor.height.toDp() }),
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
        ) {}
    }
}

private fun Rect.toDragBounds(): MagneticDragBounds =
    MagneticDragBounds(left = left, top = top, right = right, bottom = bottom)

private val AUTO_SCROLL_EDGE = 88.dp
private val AUTO_SCROLL_STEP = 11.dp
private const val AUTO_SCROLL_FRAME_MS = 16L
private const val INDICATOR_GAP_PX = 6f
