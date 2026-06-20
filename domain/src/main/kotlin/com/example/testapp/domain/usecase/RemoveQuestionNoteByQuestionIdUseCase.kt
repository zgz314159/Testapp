package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionNoteRepository
import javax.inject.Inject

class RemoveQuestionNoteByQuestionIdUseCase @Inject constructor(
    private val repo: QuestionNoteRepository
) {
    suspend operator fun invoke(questionId: Int) = repo.deleteByQuestionId(questionId)
}
