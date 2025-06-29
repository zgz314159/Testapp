package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.QuestionRepository
import javax.inject.Inject

class SaveQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(fileName: String, questions: List<Question>) {
        repository.saveQuestionsToJson(fileName, questions)
    }
}

