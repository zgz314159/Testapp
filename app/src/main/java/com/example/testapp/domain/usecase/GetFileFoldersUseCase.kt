package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.FileFolder
import com.example.testapp.domain.repository.FileFolderRepository
import javax.inject.Inject

class GetFileFoldersUseCase @Inject constructor(
    private val repo: FileFolderRepository
) {
    operator fun invoke() = repo.getAll()
}