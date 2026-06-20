package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FileFolderRepository
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val repo: FileFolderRepository
) {
    suspend operator fun invoke(name: String) = repo.deleteFolder(name)
}