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
	}.distinct()
	pinnedFileNames + visibleFileNames.filterNot(pinnedFileNames::contains)
}

fun buildRootDisplayFileNames(
	allFileNames: List<String>,
	rootVisibleFileNames: List<String>,
	primaryFileName: String,
	recentFileNames: List<String>
): List<String> = traceSection("Home.buildRootFiles") {
	if (allFileNames.isEmpty()) return@traceSection emptyList()
	// 只置顶仍在根目录的最近题库：已归入分组的文件不回流首页根列表，
	// 否则拖拽合并分组后卡片原地不动，用户会感知“分组失效”。
	reorderByRecentUsage(
		visibleFileNames = rootVisibleFileNames,
		primaryFileName = primaryFileName,
		recentFileNames = recentFileNames,
	)
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

