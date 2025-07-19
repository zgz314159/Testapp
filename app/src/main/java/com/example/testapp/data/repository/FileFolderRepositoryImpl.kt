package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FileFolderDao
import com.example.testapp.data.local.entity.FileFolderEntity
import com.example.testapp.domain.model.FileFolder
import com.example.testapp.domain.repository.FileFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FileFolderRepositoryImpl @Inject constructor(
    private val dao: FileFolderDao
) : FileFolderRepository {
    override fun getAll(): Flow<List<FileFolder>> = dao.getAll().map { list ->
        list.map { FileFolder(it.fileName, it.folderName) }
    }

    override suspend fun moveFile(fileName: String, folderName: String) {
        dao.upsert(FileFolderEntity(fileName, folderName))
    }

    override suspend fun getFolderForFile(fileName: String): String? =
        dao.getFolderForFile(fileName)
}