package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import javax.inject.Inject

class AddWrongQuestionUseCase @Inject constructor(
    private val repo: WrongBookRepository
) {
    suspend operator fun invoke(wrong: WrongQuestion) = repo.add(wrong)
}
