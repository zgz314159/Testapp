package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FileFolderRepository
import javax.inject.Inject

class RemoveFileFromFolderUseCase @Inject constructor(
    private val repo: FileFolderRepository
) {
    suspend operator fun invoke(fileName: String) = repo.removeFile(fileName)
}
