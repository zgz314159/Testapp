package com.example.testapp.di

import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.usecase.AddExamHistoryRecordUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.GradeExamUseCase
import com.example.testapp.domain.usecase.QuestionFlowCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGradeExamUseCase(
        addWrongQuestionUseCase: AddWrongQuestionUseCase,
        addExamHistoryRecordUseCase: AddExamHistoryRecordUseCase
    ): GradeExamUseCase = GradeExamUseCase(addWrongQuestionUseCase, addExamHistoryRecordUseCase)

    @Provides
    @Singleton
    fun provideGetQuestionsUseCase(repository: QuestionRepository): GetQuestionsUseCase =
        GetQuestionsUseCase(repository)

    @Provides
    @Singleton
    fun provideQuestionFlowCache(getQuestionsUseCase: GetQuestionsUseCase): QuestionFlowCache =
        QuestionFlowCache(getQuestionsUseCase)
}
