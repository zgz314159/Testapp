package com.example.testapp.di

import com.example.testapp.domain.usecase.AddExamHistoryRecordUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.GetExamHistoryListByFileUseCase
import com.example.testapp.domain.usecase.GetExamHistoryListUseCase
import com.example.testapp.domain.usecase.GetHistoryListByFileNamesUseCase
import com.example.testapp.domain.usecase.GetHistoryListByFileUseCase
import com.example.testapp.domain.usecase.GetHistoryListUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.RemoveExamHistoryByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveHistoryRecordsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.domain.usecase.SaveExamProgressUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetExamProgressFlowUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressByFileNameUseCase
import com.example.testapp.domain.usecase.ClearExamProgressByFileNameUseCase
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.PracticeProgressRepository
import com.example.testapp.domain.repository.ExamProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceUseCaseModule {

    @Provides
    @Singleton
    fun provideAddWrongQuestionUseCase(repo: com.example.testapp.domain.repository.WrongBookRepository): AddWrongQuestionUseCase =
        AddWrongQuestionUseCase(repo)

    @Provides
    @Singleton
    fun provideGetWrongBookUseCase(repo: com.example.testapp.domain.repository.WrongBookRepository): GetWrongBookUseCase =
        GetWrongBookUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveWrongQuestionsByFileNameUseCase(repo: com.example.testapp.domain.repository.WrongBookRepository): RemoveWrongQuestionsByFileNameUseCase =
        RemoveWrongQuestionsByFileNameUseCase(repo)

    @Provides
    @Singleton
    fun provideAddHistoryRecordUseCase(repo: com.example.testapp.domain.repository.HistoryRepository): AddHistoryRecordUseCase =
        AddHistoryRecordUseCase(repo)

    @Provides
    @Singleton
    fun provideGetHistoryListUseCase(repo: com.example.testapp.domain.repository.HistoryRepository): GetHistoryListUseCase =
        GetHistoryListUseCase(repo)

    @Provides
    @Singleton
    fun provideGetHistoryListByFileUseCase(repo: com.example.testapp.domain.repository.HistoryRepository): GetHistoryListByFileUseCase =
        GetHistoryListByFileUseCase(repo)

    @Provides
    @Singleton
    fun provideGetHistoryListByFileNamesUseCase(repo: com.example.testapp.domain.repository.HistoryRepository): GetHistoryListByFileNamesUseCase =
        GetHistoryListByFileNamesUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveHistoryRecordsByFileNameUseCase(repo: com.example.testapp.domain.repository.HistoryRepository): RemoveHistoryRecordsByFileNameUseCase =
        RemoveHistoryRecordsByFileNameUseCase(repo)

    @Provides
    @Singleton
    fun provideAddExamHistoryRecordUseCase(repo: com.example.testapp.domain.repository.ExamHistoryRepository): AddExamHistoryRecordUseCase =
        AddExamHistoryRecordUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveExamHistoryByFileNameUseCase(repo: com.example.testapp.domain.repository.ExamHistoryRepository): RemoveExamHistoryByFileNameUseCase =
        RemoveExamHistoryByFileNameUseCase(repo)

    @Provides
    @Singleton
    fun provideGetExamHistoryListUseCase(repo: com.example.testapp.domain.repository.ExamHistoryRepository): GetExamHistoryListUseCase =
        GetExamHistoryListUseCase(repo)

    @Provides
    @Singleton
    fun provideGetExamHistoryListByFileUseCase(repo: com.example.testapp.domain.repository.ExamHistoryRepository): GetExamHistoryListByFileUseCase =
        GetExamHistoryListByFileUseCase(repo)

    @Provides
    @Singleton
    fun provideAddFavoriteQuestionUseCase(repo: FavoriteQuestionRepository): AddFavoriteQuestionUseCase = AddFavoriteQuestionUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveFavoriteQuestionUseCase(repo: FavoriteQuestionRepository): RemoveFavoriteQuestionUseCase = RemoveFavoriteQuestionUseCase(repo)

    @Provides
    @Singleton
    fun provideGetFavoriteQuestionsUseCase(repo: FavoriteQuestionRepository): GetFavoriteQuestionsUseCase = GetFavoriteQuestionsUseCase(repo)

    @Provides
    @Singleton
    fun providePracticeProgressRepositoryUseCases(repo: PracticeProgressRepository): SavePracticeProgressUseCase = SavePracticeProgressUseCase(repo)

    @Provides
    @Singleton
    fun provideExamProgressRepositoryUseCases(repo: ExamProgressRepository): SaveExamProgressUseCase = SaveExamProgressUseCase(repo)

    @Provides
    @Singleton
    fun provideGetPracticeProgressFlowUseCase(repo: PracticeProgressRepository): GetPracticeProgressFlowUseCase = GetPracticeProgressFlowUseCase(repo)

    @Provides
    @Singleton
    fun provideGetExamProgressFlowUseCase(repo: ExamProgressRepository): GetExamProgressFlowUseCase = GetExamProgressFlowUseCase(repo)

    @Provides
    @Singleton
    fun provideClearPracticeProgressUseCase(repo: PracticeProgressRepository): ClearPracticeProgressUseCase = ClearPracticeProgressUseCase(repo)

    @Provides
    @Singleton
    fun provideClearExamProgressUseCase(repo: ExamProgressRepository): ClearExamProgressUseCase = ClearExamProgressUseCase(repo)

    @Provides
    @Singleton
    fun provideClearPracticeProgressByFileNameUseCase(repo: PracticeProgressRepository): ClearPracticeProgressByFileNameUseCase = ClearPracticeProgressByFileNameUseCase(repo)

    @Provides
    @Singleton
    fun provideClearExamProgressByFileNameUseCase(repo: ExamProgressRepository): ClearExamProgressByFileNameUseCase = ClearExamProgressByFileNameUseCase(repo)
}
