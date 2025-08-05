package com.example.testapp.presentation.component

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class ActionModeTextToolbar(
    private val view: View,
    private val onAIQuestion: () -> Unit,
    private val aiServiceName: String = "DeepSeek"
) : TextToolbar {

    private var actionMode: ActionMode? = null

    override val status: TextToolbarStatus
        get() = if (actionMode != null) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        actionMode?.finish()
        actionMode = view.startActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                var order = 0
                if (onCopyRequested != null) {
                    menu?.add(Menu.NONE, android.R.id.copy, order++, android.R.string.copy)
                }
                if (onCutRequested != null) {
                    menu?.add(Menu.NONE, android.R.id.cut, order++, android.R.string.cut)
                }
                if (onPasteRequested != null) {
                    menu?.add(Menu.NONE, android.R.id.paste, order++, android.R.string.paste)
                }
                if (onSelectAllRequested != null) {
                    menu?.add(Menu.NONE, android.R.id.selectAll, order++, android.R.string.selectAll)
                }
                menu?.add(Menu.NONE, MENU_AI_QUESTION, order, "${aiServiceName}提问")
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when (item?.itemId) {
                    android.R.id.copy -> { onCopyRequested?.invoke(); mode?.finish(); return true }
                    android.R.id.cut -> { onCutRequested?.invoke(); mode?.finish(); return true }
                    android.R.id.paste -> { onPasteRequested?.invoke(); mode?.finish(); return true }
                    android.R.id.selectAll -> { onSelectAllRequested?.invoke(); mode?.finish(); return true }
                    MENU_AI_QUESTION -> { onAIQuestion(); mode?.finish(); return true }
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                actionMode = null
            }
        }, ActionMode.TYPE_FLOATING)
    }

    override fun hide() {
        actionMode?.finish()
        actionMode = null
    }

    private companion object {
        const val MENU_AI_QUESTION = 0xDEAD
    }
}