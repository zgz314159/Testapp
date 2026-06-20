package com.example.testapp.presentation.screen.home.components

import com.example.testapp.uicommon.util.traceSection

fun reorderByRecentUsage(
	visibleFileNames: List<String>,
	primaryFileName: String,
	recentFileNames: List<String>
): List<String> = traceSection("Home.reorderRecentFiles") {
	if (visibleFileNames.isEmpty()) return@traceSection emptyList()
	val pinnedFileNames = buildList {
		if (primaryFileName.isNotBlank() && primaryFileName in visibleFileNames) {
			add(primaryFileName)
		}
		addAll(recentFileNames.filter { it in visibleFileNames && it != primaryFileName })
	}
	pinnedFileNames + visibleFileNames.filterNot(pinnedFileNames::contains)
}

fun buildRootDisplayFileNames(
	allFileNames: List<String>,
	rootVisibleFileNames: List<String>,
	primaryFileName: String,
	recentFileNames: List<String>
): List<String> = traceSection("Home.buildRootFiles") {
	if (allFileNames.isEmpty()) return@traceSection emptyList()
	val prioritizedRecentFiles = buildList {
		if (primaryFileName.isNotBlank() && primaryFileName in allFileNames) {
			add(primaryFileName)
		}
		addAll(recentFileNames.filter { it in allFileNames && it != primaryFileName })
	}.distinct().take(3)
	prioritizedRecentFiles + rootVisibleFileNames.filterNot(prioritizedRecentFiles::contains)
}

fun buildFolderFileCounts(
	fileNames: List<String>,
	folders: Map<String, String?>,
	folderNames: List<String>
): Map<String, Int> = traceSection("Home.buildFolderCounts") {
	folderNames.distinct().associateWith { folderName -> fileNames.count { folders[it] == folderName } }
}

fun filterVisibleHomeFolders(
	visibleFolderCards: List<String>,
	folderFileCounts: Map<String, Int>
): List<String> = traceSection("Home.filterVisibleFolders") {
	visibleFolderCards.filter { (folderFileCounts[it] ?: 0) > 0 }
}

