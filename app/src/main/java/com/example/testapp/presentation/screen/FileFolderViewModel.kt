package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.FileFolder
import com.example.testapp.domain.usecase.GetFileFoldersUseCase
import com.example.testapp.domain.usecase.MoveFileToFolderUseCase
import com.example.testapp.domain.usecase.GetFoldersUseCase
import com.example.testapp.domain.usecase.AddFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileFolderViewModel @Inject constructor(
    private val getFileFoldersUseCase: GetFileFoldersUseCase,
    private val moveFileToFolderUseCase: MoveFileToFolderUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val addFolderUseCase: AddFolderUseCase
) : ViewModel() {
    private val _folders = MutableStateFlow<Map<String, String>>(emptyMap())
    val folders: StateFlow<Map<String, String>> = _folders.asStateFlow()

    private val _folderNames = MutableStateFlow<List<String>>(emptyList())
    val folderNames: StateFlow<List<String>> = _folderNames.asStateFlow()


    init {
        viewModelScope.launch {
            getFileFoldersUseCase().collect { list ->
                _folders.value = list.associate { it.fileName to it.folderName }
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
}