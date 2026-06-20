package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.WrongBookRepository
import javax.inject.Inject

class GetWrongBookUseCase @Inject constructor(
    private val repo: WrongBookRepository
) {
    operator fun invoke() = repo.getAll()
}
