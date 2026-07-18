package com.example.testapp.uicommon.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeRevealActionBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    revealWidth: Dp = 92.dp,
    background: @Composable BoxScope.((animateClosed: Boolean) -> Unit) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val settleAnim = remember { Animatable(0f) }
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val enabledState = rememberUpdatedState(enabled)
    val closeActionState = rememberUpdatedState<(Boolean) -> Unit> { animateClosed ->
        scope.launch {
            if (animateClosed) {
                settleAnim.snapTo(offsetX)
                settleAnim.animateTo(0f, animationSpec = tween(160)) {
                    offsetX = value
                }
            } else {
                offsetX = 0f
            }
        }
    }
    val actionWidthPx = with(density) { revealWidth.toPx() }

    // enabled 翻转时不得拆除 pointerInput/offset，否则会取消子节点正在进行的长按拖拽。
    LaunchedEffect(enabled) {
        if (!enabled && offsetX != 0f) {
            offsetX = 0f
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(actionWidthPx, viewConfiguration.touchSlop) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (!enabledState.value) return@awaitEachGesture

                        var totalDx = 0f
                        var totalDy = 0f
                        var horizontalLocked = false
                        var released = false

                        while (!released) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()
                            if (change == null) {
                                released = true
                                continue
                            }

                            if (!change.pressed) {
                                released = true
                                continue
                            }

                            val delta = change.position - change.previousPosition

                            if (!horizontalLocked) {
                                totalDx += delta.x
                                totalDy += delta.y

                                if (abs(totalDy) > viewConfiguration.touchSlop && abs(totalDy) >= abs(totalDx)) {
                                    return@awaitEachGesture
                                }

                                if (abs(totalDx) > viewConfiguration.touchSlop && abs(totalDx) > abs(totalDy) * 1.25f) {
                                    horizontalLocked = true
                                }
                            }

                            if (horizontalLocked) {
                                change.consume()
                                // 同步跟手，避免每个 pointer delta 额外 launch 协程。
                                offsetX = (offsetX + delta.x).coerceIn(-actionWidthPx, 0f)
                            }
                        }

                        if (horizontalLocked) {
                            val snapTarget = if (-offsetX >= actionWidthPx / 2f) -actionWidthPx else 0f
                            scope.launch {
                                settleAnim.snapTo(offsetX)
                                settleAnim.animateTo(snapTarget, animationSpec = tween(180)) {
                                    offsetX = value
                                }
                            }
                        }
                    }
                }
                .offset { IntOffset(offsetX.roundToInt(), 0) },
        ) {
            content()
        }

        if (enabled && offsetX < -1f) {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.CenterEnd)
                    .width(revealWidth)
                    .fillMaxHeight(),
                content = { background(closeActionState.value) }
            )
        }
    }
}
