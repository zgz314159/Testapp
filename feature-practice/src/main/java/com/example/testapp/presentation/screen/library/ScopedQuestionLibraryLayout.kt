package com.example.testapp.presentation.screen.library

data class ScopedQuestionLibraryLayout(
    val scopedFolders: Map<String, String>,
    val scopedFolderNames: List<String>,
    val rootDisplayFileNames: List<String>,
    val currentFolderDisplayFileNames: List<String>,
    val visibleFolderCards: List<String>,
    val folderFileCounts: Map<String, Int>
)

fun buildScopedQuestionLibraryLayout(
    scope: String,
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>,
    currentFolder: String?
): ScopedQuestionLibraryLayout {
    val scopedFolders = fileNames.associateWith { name ->
        folders[scopedLibraryName(scope, name)]
            ?.takeIf { isScopedLibraryName(scope, it) }
            ?.let { unscopedLibraryName(scope, it) }
    }.filterValues { it != null }.mapValues { it.value.orEmpty() }

    val scopedFolderNames = folderNames
        .filter { isScopedLibraryName(scope, it) }
        .map { unscopedLibraryName(scope, it) }

    val rootDisplayFileNames = fileNames.filter { scopedFolders[it] == null }
    val currentFolderDisplayFileNames = currentFolder?.let { folder ->
        fileNames.filter { scopedFolders[it] == folder }
    }.orEmpty()

    val visibleFolderCards = scopedFolderNames.distinct()
        .filter { folder -> fileNames.any { scopedFolders[it] == folder } }

    val folderFileCounts = scopedFolderNames.distinct()
        .associateWith { folder -> fileNames.count { scopedFolders[it] == folder } }

    return ScopedQuestionLibraryLayout(
        scopedFolders = scopedFolders,
        scopedFolderNames = scopedFolderNames,
        rootDisplayFileNames = rootDisplayFileNames,
        currentFolderDisplayFileNames = currentFolderDisplayFileNames,
        visibleFolderCards = visibleFolderCards,
        folderFileCounts = folderFileCounts
    )
}
