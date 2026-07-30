package com.example.testapp.presentation.screen.magnetic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.session.magnetic.MagneticSemanticRole
import com.example.testapp.presentation.session.magnetic.MagneticToken
import com.example.testapp.uicommon.design.AppThemeColors

@Composable
fun MagneticCandidateToken(
    token: MagneticToken,
    hinted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = if (hinted) AppThemeColors.warningSoft else colors.surface,
        border = BorderStroke(if (hinted) 1.5.dp else 1.dp, if (hinted) colors.primary else colors.outline),
    ) {
        TokenTextContent(token)
    }
}

@Composable
fun MagneticPlacedToken(
    token: MagneticToken,
    index: Int,
    total: Int,
    connected: Boolean,
    hinted: Boolean,
    onReturn: () -> Unit,
    onMove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dragging: Boolean = false,
    onBoundsChanged: (Rect) -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Surface(
        onClick = { if (!dragging) onReturn() },
        modifier =
            modifier
                .onGloballyPositioned { coordinates -> onBoundsChanged(coordinates.boundsInWindow()) }
                .alpha(if (dragging) DRAG_PLACEHOLDER_ALPHA else 1f)
                .graphicsLayer {
                    scaleX = if (dragging) 0.97f else 1f
                    scaleY = if (dragging) 0.97f else 1f
                }.shadow(if (connected) 5.dp else 2.dp, shape)
                .semantics {
                    customActions =
                        buildList {
                            if (index > 0) {
                                add(CustomAccessibilityAction("前移") { onMove(index - 1); true })
                            }
                            if (index < total - 1) {
                                add(CustomAccessibilityAction("后移") { onMove(index + 1); true })
                            }
                            add(CustomAccessibilityAction("撤回候选区") { onReturn(); true })
                        }
                }.pointerInput(token.id, index, total) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = onDragStart,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        },
                        onDragCancel = onDragCancel,
                        onDragEnd = onDragEnd,
                    )
                },
        shape = shape,
        color =
            when {
                hinted -> AppThemeColors.warningSoft
                connected -> colors.primaryContainer
                else -> colors.surface
            },
        border =
            BorderStroke(
                if (connected || hinted) 1.5.dp else 1.dp,
                if (connected || hinted) colors.primary else colors.outline,
            ),
    ) {
        TokenTextContent(token)
    }
}

@Composable
internal fun MagneticFloatingToken(
    token: MagneticToken,
    hinted: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier =
            Modifier
                .graphicsLayer {
                    scaleX = FLOATING_TOKEN_SCALE
                    scaleY = FLOATING_TOKEN_SCALE
                }.shadow(12.dp, shape),
        shape = shape,
        color = if (hinted) AppThemeColors.warningSoft else colors.primaryContainer,
        border = BorderStroke(2.dp, colors.primary),
        tonalElevation = 6.dp,
    ) {
        TokenTextContent(token)
    }
}

@Composable
private fun TokenTextContent(token: MagneticToken) {
    Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
        Text(
            text = token.text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (token.role != MagneticSemanticRole.OTHER) {
            Text(
                text = roleLabel(token.role),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
            )
        }
    }
}

private fun roleLabel(role: MagneticSemanticRole): String =
    when (role) {
        MagneticSemanticRole.SUBJECT -> "主体"
        MagneticSemanticRole.CONDITION -> "条件"
        MagneticSemanticRole.MODAL -> "强制词"
        MagneticSemanticRole.ACTION -> "动作"
        MagneticSemanticRole.OBJECT -> "对象"
        MagneticSemanticRole.NUMBER -> "参数"
        MagneticSemanticRole.PUNCTUATION -> "连接"
        MagneticSemanticRole.OTHER -> ""
    }

private const val DRAG_PLACEHOLDER_ALPHA = 0.18f
private const val FLOATING_TOKEN_SCALE = 1.06f
