package com.example.testapp.presentation.screen.questionbank

data class QuestionBankExpansionSnapshot(
    val isSearchMode: Boolean = false,
    val expandedFiles: Set<String> = emptySet(),
    val expandedFolders: Set<String> = emptySet(),
    val searchCollapsedFiles: Set<String> = emptySet(),
    val searchCollapsedFolders: Set<String> = emptySet()
) {
    fun isFileExpanded(fileName: String): Boolean =
        if (isSearchMode) fileName !in searchCollapsedFiles else fileName in expandedFiles

    fun isFolderExpanded(folderName: String): Boolean =
        if (isSearchMode) folderName !in searchCollapsedFolders else folderName in expandedFolders
}
