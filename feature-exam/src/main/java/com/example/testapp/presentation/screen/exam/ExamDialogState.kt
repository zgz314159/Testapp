package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for 10+ dialog/section toggle variables in ExamScreen.
 * Removes 10 `var ... by remember { mutableStateOf(...) }` declarations.
 */
class ExamDialogState {
    var showList by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    var deleteTarget by mutableStateOf("")
    var showDeleteNoteDialog by mutableStateOf(false)
    var aiMenuExpanded by mutableStateOf(false)
    var menuExpanded by mutableStateOf(false)
    var showEditQuestionDialog by mutableStateOf(false)
    var editedQuestionContent by mutableStateOf("")
    var editedQuestionAnswer by mutableStateOf("")
    var editedAnswerParts by mutableStateOf(listOf<String>())
    var showChatGptDialog by mutableStateOf(false)
    var showExitDialog by mutableStateOf(false)
    var expandedSection by mutableStateOf(-1)
}
