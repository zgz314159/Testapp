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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.session.magnetic.MagneticSemanticRole
import com.example.testapp.presentation.session.magnetic.MagneticToken
import kotlin.math.roundToInt

private val TokenBlue = Color(0xFF4F7EDC)
private val TokenBlueSoft = Color(0xFFEAF1FF)
private val TokenHint = Color(0xFFFFF4D6)
private val TokenText = Color(0xFF22304A)

@Composable
fun MagneticCandidateToken(
    token: MagneticToken,
    hinted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = if (hinted) TokenHint else Color.White,
        border = BorderStroke(if (hinted) 1.5.dp else 1.dp, if (hinted) TokenBlue else Color(0xFFD6DCE8)),
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
) {
    var dragX by remember(token.id, index) { mutableFloatStateOf(0f) }
    val thresholdPx = with(LocalDensity.current) { 68.dp.toPx() }
    val shape = RoundedCornerShape(14.dp)
    Surface(
        onClick = onReturn,
        modifier =
            modifier
                .graphicsLayer {
                    translationX = dragX
                    scaleX = if (dragX == 0f) 1f else 1.04f
                    scaleY = if (dragX == 0f) 1f else 1.04f
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
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragX += dragAmount.x
                        },
                        onDragCancel = { dragX = 0f },
                        onDragEnd = {
                            val steps = (dragX / thresholdPx).roundToInt()
                            if (steps != 0) {
                                onMove((index + steps).coerceIn(0, total - 1))
                            }
                            dragX = 0f
                        },
                    )
                },
        shape = shape,
        color =
            when {
                hinted -> TokenHint
                connected -> TokenBlueSoft
                else -> Color.White
            },
        border =
            BorderStroke(
                if (connected || hinted) 1.5.dp else 1.dp,
                if (connected || hinted) TokenBlue else Color(0xFFD6DCE8),
            ),
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
            color = TokenText,
        )
        if (token.role != MagneticSemanticRole.OTHER) {
            Text(
                text = roleLabel(token.role),
                fontSize = 10.sp,
                color = TokenBlue.copy(alpha = 0.82f),
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
