package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.WrongBookRepository
import javax.inject.Inject

class RemoveWrongQuestionsByFileNameUseCase @Inject constructor(
    private val repo: WrongBookRepository
) {
    suspend operator fun invoke(fileName: String) = repo.removeByFileName(fileName)
}