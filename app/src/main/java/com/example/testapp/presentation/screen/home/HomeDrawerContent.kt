package com.example.testapp.presentation.screen.home

import androidx.compose.runtime.Composable
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.questionbank.QuestionBankDrawer

@Composable
fun HomeDrawerContent(
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>,
    fileStatistics: Map<String, FileStatistics>,
    onQuestionSelected: (String, Int) -> Unit,
    onClose: () -> Unit
) {
    QuestionBankDrawer(
        fileNames = fileNames,
        folders = folders,
        folderNames = folderNames,
        fileStatistics = fileStatistics,
        onQuestionSelected = onQuestionSelected,
        onClose = onClose
    )
}
