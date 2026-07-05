package com.example.testapp.presentation.screen.home

enum class HomeLibraryEmptyReason {
    NO_QUIZ_FILES,
    ROOT_EMPTY_WITH_FOLDERS
}

fun resolveHomeLibraryEmpty(
    currentFolder: String?,
    displayFileNames: List<String>,
    visibleFolders: List<String>,
    totalFileCount: Int
): HomeLibraryEmptyReason? {
    if (currentFolder != null) return null
    if (displayFileNames.isNotEmpty() || visibleFolders.isNotEmpty()) return null
    return if (totalFileCount == 0) {
        HomeLibraryEmptyReason.NO_QUIZ_FILES
    } else {
        HomeLibraryEmptyReason.ROOT_EMPTY_WITH_FOLDERS
    }
}
