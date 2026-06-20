package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionAskRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryFacade @Inject constructor(
    val questions: QuestionRepository,
    val wrongBook: WrongBookRepository,
    val history: HistoryRepository,
    val favorites: FavoriteQuestionRepository,
    val analysis: QuestionAnalysisRepository,
    val asks: QuestionAskRepository,
    val notes: QuestionNoteRepository
)
