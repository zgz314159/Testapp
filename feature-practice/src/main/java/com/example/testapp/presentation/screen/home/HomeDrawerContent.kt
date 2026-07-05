package com.example.testapp.presentation.screen.home

import androidx.compose.runtime.Composable
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.questionbank.QuestionBankDrawer
import com.example.testapp.presentation.screen.questionbank.QuestionBankDrawerViewModel

@Composable
fun HomeDrawerContent(
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>,
    fileStatistics: Map<String, FileStatistics>,
    drawerViewModel: QuestionBankDrawerViewModel,
    onQuestionSelected: (String, Int, String) -> Unit,
    onQuestionEdit: (String, Int, String) -> Unit = { _, _, _ -> },
    onClose: () -> Unit,
) {
    QuestionBankDrawer(
        fileNames = fileNames,
        folders = folders,
        folderNames = folderNames,
        fileStatistics = fileStatistics,
        onQuestionSelected = onQuestionSelected,
        onQuestionEdit = onQuestionEdit,
        onClose = onClose,
        viewModel = drawerViewModel,
    )
}
