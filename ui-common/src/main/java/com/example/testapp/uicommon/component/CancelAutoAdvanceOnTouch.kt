package com.example.testapp.uicommon.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.cancelAutoAdvanceOnTouch(onCancel: () -> Unit): Modifier =
    this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.changes.any { it.pressed }) {
                    onCancel()
                }
            }
        }
    }
