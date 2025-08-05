package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FileFolderRepository
import javax.inject.Inject

class MoveFileToFolderUseCase @Inject constructor(
    private val repo: FileFolderRepository
) {
    suspend operator fun invoke(fileName: String, folderName: String) =
        repo.moveFile(fileName, folderName)
}