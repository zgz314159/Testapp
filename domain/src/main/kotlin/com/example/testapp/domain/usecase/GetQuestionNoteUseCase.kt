package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionNoteRepository
import javax.inject.Inject

class GetQuestionNoteUseCase @Inject constructor(
    private val repository: QuestionNoteRepository
) {
    suspend operator fun invoke(questionId: Int): Result<String?> = runCatching {
        repository.getNote(questionId)
    }
}

class SaveQuestionNoteUseCase @Inject constructor(
    private val repository: QuestionNoteRepository
) {
    suspend operator fun invoke(questionId: Int, note: String): Result<Unit> = runCatching {
        repository.saveNote(questionId, note)
    }
}