package com.example.testapp.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.home.components.buildFolderFileCounts
import com.example.testapp.presentation.screen.home.components.buildRootDisplayFileNames
import com.example.testapp.presentation.screen.home.components.filterVisibleHomeFolders
import com.example.testapp.presentation.screen.home.components.reorderByRecentUsage

data class HomeLibraryDisplayState(
    val rootDisplayFileNames: List<String>,
    val currentFolderDisplayFileNames: List<String>,
    val folderFileCounts: Map<String, Int>,
    val visibleHomeFolders: List<String>,
    val currentFolderFileNames: Set<String>,
    val displayFileNames: List<String>,
    val displayFolders: List<String>,
    val homeLibraryEmptyReason: HomeLibraryEmptyReason?,
)

data class HomeNavPrefsState(
    val bottomNavIndex: Int,
    val setBottomNavIndex: (Int) -> Unit,
)

@Composable
fun rememberHomeLibraryDisplayState(
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>,
    currentFolder: String?,
    storedFileName: String,
    recentFileNames: List<String>,
): HomeLibraryDisplayState {
    val rootDisplayFileNames = remember(fileNames, folders, storedFileName, recentFileNames) {
        buildRootDisplayFileNames(
            allFileNames = fileNames,
            rootVisibleFileNames = fileNames.filter { folders[it] == null },
            primaryFileName = storedFileName,
            recentFileNames = recentFileNames,
        )
    }
    val currentFolderDisplayFileNames = remember(fileNames, folders, currentFolder, storedFileName, recentFileNames) {
        currentFolder?.let { folderName ->
            reorderByRecentUsage(
                visibleFileNames = fileNames.filter { folders[it] == folderName },
                primaryFileName = storedFileName,
                recentFileNames = recentFileNames,
            )
        } ?: emptyList()
    }
    val folderFileCounts = remember(fileNames, folders, folderNames) {
        buildFolderFileCounts(fileNames, folders, folderNames)
    }
    val visibleHomeFolders = remember(folderNames, folderFileCounts) {
        filterVisibleHomeFolders(folderNames.distinct(), folderFileCounts)
    }
    val currentFolderFileNames = remember(currentFolderDisplayFileNames) {
        currentFolderDisplayFileNames.toSet()
    }
    val displayFileNames = if (currentFolder == null) rootDisplayFileNames else currentFolderDisplayFileNames
    val displayFolders = if (currentFolder == null) visibleHomeFolders else emptyList()
    val homeLibraryEmptyReason = remember(currentFolder, displayFileNames, displayFolders, fileNames) {
        resolveHomeLibraryEmpty(
            currentFolder = currentFolder,
            displayFileNames = displayFileNames,
            visibleFolders = displayFolders,
            totalFileCount = fileNames.size,
        )
    }
    return HomeLibraryDisplayState(
        rootDisplayFileNames = rootDisplayFileNames,
        currentFolderDisplayFileNames = currentFolderDisplayFileNames,
        folderFileCounts = folderFileCounts,
        visibleHomeFolders = visibleHomeFolders,
        currentFolderFileNames = currentFolderFileNames,
        displayFileNames = displayFileNames,
        displayFolders = displayFolders,
        homeLibraryEmptyReason = homeLibraryEmptyReason,
    )
}

@Composable
fun rememberHomeNavPrefsState(): HomeNavPrefsState {
    val context = LocalContext.current
    val storedNavIndex by FontSettingsDataStore
        .getLastSelectedNav(context)
        .collectAsState(initial = 0)
    var bottomNavIndex by remember { mutableStateOf(storedNavIndex.coerceIn(0, 4)) }
    LaunchedEffect(storedNavIndex) { bottomNavIndex = storedNavIndex.coerceIn(0, 4) }
    LaunchedEffect(bottomNavIndex) { FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex) }
    return HomeNavPrefsState(
        bottomNavIndex = bottomNavIndex,
        setBottomNavIndex = { bottomNavIndex = it },
    )
}
