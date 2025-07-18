package com.example.testapp.presentation.component

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class CustomTextToolbar(private val onShow: (Callbacks) -> Unit) : TextToolbar {
    data class Callbacks(
        val onCopy: (() -> Unit)? = null,
        val onCut: (() -> Unit)? = null,
        val onSelectAll: (() -> Unit)? = null
    )
    private var shown = false
    override val status: TextToolbarStatus
        get() = if (shown) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        shown = true
        onShow(Callbacks(onCopyRequested, onCutRequested, onSelectAllRequested))
    }

    override fun hide() {
        shown = false
    }
}