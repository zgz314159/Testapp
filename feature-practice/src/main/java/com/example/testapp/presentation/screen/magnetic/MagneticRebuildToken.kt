package com.example.testapp.presentation.screen.magnetic

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppThemeColors

private val TokenCorner = RoundedCornerShape(16.dp)

@Composable
fun MagneticCandidateToken(
    token: MagneticToken,
    hinted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    MagneticTokenSurface(
        onClick = onClick,
        enabled = true,
        containerColor = if (hinted) AppThemeColors.warningSoft else colors.surface,
        elevationBoost = hinted,
        modifier = modifier,
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
    MagneticTokenSurface(
        onClick = { if (!dragging) onReturn() },
        enabled = !dragging,
        containerColor =
            when {
                hinted -> AppThemeColors.warningSoft
                connected -> colors.primaryContainer
                else -> colors.surface
            },
        elevationBoost = connected || hinted,
        modifier =
            modifier
                .onGloballyPositioned { coordinates -> onBoundsChanged(coordinates.boundsInWindow()) }
                .alpha(if (dragging) DRAG_PLACEHOLDER_ALPHA else 1f)
                .graphicsLayer {
                    scaleX = if (dragging) 0.97f else 1f
                    scaleY = if (dragging) 0.97f else 1f
                }.semantics {
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
    Surface(
        modifier =
            Modifier.graphicsLayer {
                scaleX = FLOATING_TOKEN_SCALE
                scaleY = FLOATING_TOKEN_SCALE
            },
        shape = TokenCorner,
        color = if (hinted) AppThemeColors.warningSoft else colors.primaryContainer,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
    ) {
        TokenTextContent(token)
    }
}

/** Matches [com.example.testapp.uicommon.design.QuestionOptionSurface] elev/press recipe. */
@Composable
private fun MagneticTokenSurface(
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    elevationBoost: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "magneticTokenScale",
    )
    val elevation by animateDpAsState(
        targetValue =
            when {
                pressed -> 2.dp
                elevationBoost -> 9.dp
                else -> 7.dp
            },
        label = "magneticTokenElevation",
    )
    Surface(
        onClick = onClick,
        modifier =
            modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = TokenCorner,
        color = containerColor,
        tonalElevation = 2.dp,
        shadowElevation = elevation,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

@Composable
private fun TokenTextContent(token: MagneticToken) {
    val tokens = AppElevatedActionSheetTokens
    Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
        Text(
            text = token.text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = tokens.textPrimary,
        )
        if (token.role != MagneticSemanticRole.OTHER) {
            Text(
                text = roleLabel(token.role),
                fontSize = 10.sp,
                color = tokens.brandBlue.copy(alpha = 0.88f),
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
private const val FLOATING_TOKEN_SCALE = 1.01f
