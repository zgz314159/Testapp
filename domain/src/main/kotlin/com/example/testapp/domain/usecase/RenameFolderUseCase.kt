package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FileFolderRepository
import javax.inject.Inject

class RenameFolderUseCase @Inject constructor(
    private val repo: FileFolderRepository
) {
    suspend operator fun invoke(oldName: String, newName: String) =
        repo.renameFolder(oldName, newName)
}