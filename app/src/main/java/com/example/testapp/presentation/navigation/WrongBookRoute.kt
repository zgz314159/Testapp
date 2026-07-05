package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.wrongbook.WrongBookScreen

@Composable
fun WrongBookRoute(
    fileName: String? = null,
    onBack: () -> Unit,
    onOpenFile: (String) -> Unit,
    onStartWrongBookQuiz: (String) -> Unit,
) {
    WrongBookScreen(
        fileName = fileName,
        folderViewModel = hiltViewModel(),
        dragViewModel = hiltViewModel(),
        onBack = onBack,
        onOpenFile = onOpenFile,
        onStartWrongBookQuiz = onStartWrongBookQuiz,
    )
}
