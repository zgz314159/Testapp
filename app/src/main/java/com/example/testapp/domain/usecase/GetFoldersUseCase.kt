package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FileFolderRepository
import javax.inject.Inject

class GetFoldersUseCase @Inject constructor(
    private val repo: FileFolderRepository
) {
    operator fun invoke() = repo.getFolderNames()
}