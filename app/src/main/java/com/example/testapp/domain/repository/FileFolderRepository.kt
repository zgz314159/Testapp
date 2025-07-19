package com.example.testapp.domain.repository

import com.example.testapp.domain.model.FileFolder
import kotlinx.coroutines.flow.Flow

interface FileFolderRepository {
    fun getAll(): Flow<List<FileFolder>>
    suspend fun moveFile(fileName: String, folderName: String)
    suspend fun getFolderForFile(fileName: String): String?
    fun getFolderNames(): Flow<List<String>>
    suspend fun addFolder(name: String)
    suspend fun renameFolder(oldName: String, newName: String)
    suspend fun deleteFolder(name: String)

}