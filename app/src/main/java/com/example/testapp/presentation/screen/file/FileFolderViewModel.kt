package com.example.testapp.presentation.screen.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.FileFolder
import com.example.testapp.domain.usecase.GetFileFoldersUseCase
import com.example.testapp.domain.usecase.MoveFileToFolderUseCase
import com.example.testapp.domain.usecase.GetFoldersUseCase
import com.example.testapp.domain.usecase.AddFolderUseCase
import com.example.testapp.domain.usecase.RenameFolderUseCase
    import com.example.testapp.domain.usecase.DeleteFolderUseCase
import com.example.testapp.domain.usecase.RemoveFileFromFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FileFolderViewModel @Inject constructor(
    private val getFileFoldersUseCase: GetFileFoldersUseCase,
    private val moveFileToFolderUseCase: MoveFileToFolderUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val addFolderUseCase: AddFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val removeFileFromFolderUseCase: RemoveFileFromFolderUseCase
) : ViewModel() {
    private data class ScopedName(val scope: String?, val value: String)

    private val _folders = MutableStateFlow<Map<String, String>>(emptyMap())
    val folders: StateFlow<Map<String, String>> = _folders.asStateFlow()

    private val _folderNames = MutableStateFlow<List<String>>(emptyList())
    val folderNames: StateFlow<List<String>> = _folderNames.asStateFlow()

    init {
        viewModelScope.launch {
            getFileFoldersUseCase().collect { list ->
                val map = list.associate { it.fileName to it.folderName }
                _folders.value = map
            }
        }
        viewModelScope.launch {
            getFoldersUseCase().collect { names ->
                
                _folderNames.value = names
            }
        }
    }

    fun moveFile(fileName: String, folderName: String) {
        viewModelScope.launch { moveFileToFolderUseCase(fileName, folderName) }
    }

    fun addFolder(name: String) {
        
        viewModelScope.launch { addFolderUseCase(name) }
    }

    fun renameFolder(oldName: String, newName: String) {
        
        viewModelScope.launch { renameFolderUseCase(oldName, newName) }
    }

    fun deleteFolder(name: String) {
        
        viewModelScope.launch { deleteFolderUseCase(name) }
    }

    fun removeFileFromFolder(fileName: String) {
        viewModelScope.launch { removeFileFromFolderUseCase(fileName) }
    }

    fun groupFiles(draggedFileName: String, targetFileName: String) {
        viewModelScope.launch {
            groupFilesInternal(draggedFileName, targetFileName)
        }
    }

    private suspend fun groupFilesInternal(draggedFileName: String, targetFileName: String) {
        if (draggedFileName == targetFileName) return
        val destinationFolder = buildGroupedFolderName(draggedFileName, targetFileName)
        if (destinationFolder !in _folderNames.value) {
            addFolderUseCase(destinationFolder)
        }
        moveFileToFolderUseCase(targetFileName, destinationFolder)
        moveFileToFolderUseCase(draggedFileName, destinationFolder)
    }

    private fun buildGroupedFolderName(draggedFileName: String, targetFileName: String): String {
        val draggedScoped = splitScopedName(draggedFileName)
        val targetScoped = splitScopedName(targetFileName)
        val scope = when {
            draggedScoped.scope == targetScoped.scope -> draggedScoped.scope
            else -> null
        }
        val draggedBase = draggedScoped.value.substringBeforeLast('.').trim()
        val targetBase = targetScoped.value.substringBeforeLast('.').trim()
        val commonPrefix = draggedBase.commonPrefixWith(targetBase).trim { it == '-' || it == '_' || it == ' ' || it == '（' || it == '(' }
        val rawBase = when {
            commonPrefix.length >= 4 -> commonPrefix
            targetBase.isNotBlank() && draggedBase.isNotBlank() -> "$targetBase 等"
            targetBase.isNotBlank() -> targetBase
            draggedBase.isNotBlank() -> draggedBase
            else -> "新建文件夹"
        }
        val base = if (rawBase.endsWith("分组")) rawBase else "$rawBase 分组"

        val existing = _folderNames.value.toSet()
        val scopedBase = applyScope(scope, base)
        if (scopedBase !in existing) return scopedBase

        var suffix = 2
        while (true) {
            val candidate = applyScope(scope, String.format(Locale.ROOT, "%s_%d", base, suffix))
            if (candidate !in existing) return candidate
            suffix += 1
        }
    }

    private fun splitScopedName(name: String): ScopedName {
        val separatorIndex = name.indexOf("::")
        if (separatorIndex <= 0) return ScopedName(scope = null, value = name)

        val prefix = name.substring(0, separatorIndex)
        return if (prefix.startsWith("__") && prefix.endsWith("__")) {
            ScopedName(scope = prefix, value = name.substring(separatorIndex + 2))
        } else {
            ScopedName(scope = null, value = name)
        }
    }

    private fun applyScope(scope: String?, value: String): String {
        return if (scope.isNullOrBlank()) value else "$scope::$value"
    }
}