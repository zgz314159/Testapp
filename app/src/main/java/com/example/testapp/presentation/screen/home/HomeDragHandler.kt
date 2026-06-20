package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class HomeDropTarget(val file: String? = null, val folder: String? = null)

class HomeRootCoordsRef {
    var value: LayoutCoordinates? = null
}

class HomeDragFinishRef {
    var handledByCard = false
}

fun updateHomeDragHover(
    dragViewModel: DragDropViewModel,
    folderBounds: SnapshotStateMap<String, Rect>,
    fileCardBounds: SnapshotStateMap<String, Rect>,
    position: Offset,
    currentFolder: String? = null,
    homeDropTargetKey: String = "__HOME_ROOT__",
    currentFolderFileNames: Set<String> = emptySet()
) {
    val draggingFile = dragViewModel.draggingFile.value
    dragViewModel.updatePosition(position)
    if (currentFolder == null) {
        val folder = folderBounds.entries.find { it.value.contains(position) }?.key
        val file = fileCardBounds.entries.firstOrNull {
            it.key != draggingFile && it.value.contains(position)
        }?.key
        dragViewModel.setHoverFolder(folder)
        dragViewModel.setHoverFile(file)
    } else {
        dragViewModel.setHoverFolder(
            folderBounds[homeDropTargetKey]?.takeIf { it.contains(position) }?.let { homeDropTargetKey }
        )
        dragViewModel.setHoverFile(
            fileCardBounds.entries.firstOrNull {
                it.key != draggingFile && it.key in currentFolderFileNames && it.value.contains(position)
            }?.key
        )
    }
}

fun resolveHomeDropTarget(
    draggingFile: String,
    hoverFile: String?,
    hoverFolder: String?,
    dropPosition: Offset,
    folderBounds: Map<String, Rect>,
    fileCardBounds: Map<String, Rect>,
    currentFolder: String? = null,
    homeDropTargetKey: String = "__HOME_ROOT__",
    currentFolderFileNames: Set<String> = emptySet()
): HomeDropTarget {
    if (currentFolder == null) {
        val file = hoverFile?.takeIf { it != draggingFile }
            ?: fileCardBounds.entries
                .firstOrNull { it.key != draggingFile && it.value.contains(dropPosition) }
                ?.key
        val folder = hoverFolder
            ?: folderBounds.entries.find { it.value.contains(dropPosition) }?.key
        return HomeDropTarget(file = file, folder = folder)
    }
    val folder = hoverFolder?.takeIf { it == homeDropTargetKey }
        ?: folderBounds[homeDropTargetKey]?.takeIf { it.contains(dropPosition) }?.let { homeDropTargetKey }
    val file = hoverFile?.takeIf { it != draggingFile && it in currentFolderFileNames }
        ?: fileCardBounds.entries
            .firstOrNull {
                it.key != draggingFile && it.key in currentFolderFileNames && it.value.contains(dropPosition)
            }
            ?.key
    return HomeDropTarget(file = file, folder = folder)
}

fun finishActiveDrag(
    fileName: String,
    dragViewModel: DragDropViewModel,
    folderViewModel: FileFolderViewModel,
    folderBounds: SnapshotStateMap<String, Rect>,
    fileCardBounds: SnapshotStateMap<String, Rect>,
    coroutineScope: CoroutineScope,
    source: String,
    currentFolder: String? = null,
    homeDropTargetKey: String = "__HOME_ROOT__",
    currentFolderFileNames: Set<String> = emptySet()
) {
    val active = dragViewModel.draggingFile.value
    if (active != fileName) return
    if (!dragViewModel.tryCommitDrop()) return

    val target = resolveHomeDropTarget(
        draggingFile = fileName,
        hoverFile = dragViewModel.hoverFile.value,
        hoverFolder = dragViewModel.hoverFolder.value,
        dropPosition = dragViewModel.dragPosition.value,
        folderBounds = folderBounds,
        fileCardBounds = fileCardBounds,
        currentFolder = currentFolder,
        homeDropTargetKey = homeDropTargetKey,
        currentFolderFileNames = currentFolderFileNames
    )
    dragViewModel.endDragging()
    when {
        target.folder == homeDropTargetKey -> {
            coroutineScope.launch { folderViewModel.removeFileFromFolder(fileName) }
        }
        target.folder != null -> {
            folderViewModel.moveFile(fileName, target.folder)
        }
        target.file != null -> {
            coroutineScope.launch {
                folderViewModel.groupFiles(fileName, target.file)
            }
        }
    }
}

@Composable
fun rememberHomeRootDragModifier(
    dragViewModel: DragDropViewModel,
    folderViewModel: FileFolderViewModel,
    folderBounds: SnapshotStateMap<String, Rect>,
    fileCardBounds: SnapshotStateMap<String, Rect>,
    coordsRef: HomeRootCoordsRef,
    finishRef: HomeDragFinishRef,
    coroutineScope: CoroutineScope,
    currentFolder: String? = null,
    homeDropTargetKey: String = "__HOME_ROOT__",
    currentFolderFileNames: Set<String> = emptySet()
): Modifier {
    val draggingFile by dragViewModel.draggingFile.collectAsState()
    val currentDraggingFile by rememberUpdatedState(draggingFile)
    val currentFolderState by rememberUpdatedState(currentFolder)
    val currentFolderFileNamesState by rememberUpdatedState(currentFolderFileNames)
    val currentFinish by rememberUpdatedState<(String, String) -> Unit>({ fileName, source ->
        finishActiveDrag(
            fileName,
            dragViewModel,
            folderViewModel,
            folderBounds,
            fileCardBounds,
            coroutineScope,
            source,
            currentFolderState,
            homeDropTargetKey,
            currentFolderFileNamesState
        )
    })
    val currentHover by rememberUpdatedState<(Offset) -> Unit>({ position ->
        updateHomeDragHover(
            dragViewModel,
            folderBounds,
            fileCardBounds,
            position,
            currentFolderState,
            homeDropTargetKey,
            currentFolderFileNamesState
        )
    })

    return Modifier.pointerInput(currentFolder) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val pointerId = down.id
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId }
                    ?: event.changes.firstOrNull()
                val activeFile = currentDraggingFile
                if (change == null) {
                    if (activeFile != null && !finishRef.handledByCard) {
                        currentFinish(activeFile, "root-null-change")
                    }
                    finishRef.handledByCard = false
                    break
                }
                if (activeFile != null) {
                    val rootPosition = coordsRef.value?.localToRoot(change.position)
                        ?: change.position
                    currentHover(rootPosition)
                }
                if (!change.pressed) {
                    if (activeFile != null && !finishRef.handledByCard) {
                        currentFinish(activeFile, "root-release")
                    }
                    finishRef.handledByCard = false
                    break
                }
            }
        }
    }
}
