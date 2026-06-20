package com.example.testapp.di

import com.example.testapp.domain.usecase.GetDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.SaveDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.GetSparkAskResultUseCase
import com.example.testapp.domain.usecase.SaveSparkAskResultUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.domain.usecase.SaveSparkAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.RemoveQuestionAnalysisByQuestionIdUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
import com.example.testapp.domain.usecase.RemoveQuestionNoteByQuestionIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkUseCaseModule {

    @Provides
    @Singleton
    fun provideGetDeepSeekAskResultUseCase(repo: com.example.testapp.domain.repository.QuestionAskRepository): GetDeepSeekAskResultUseCase =
        GetDeepSeekAskResultUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveDeepSeekAskResultUseCase(repo: com.example.testapp.domain.repository.QuestionAskRepository): SaveDeepSeekAskResultUseCase =
        SaveDeepSeekAskResultUseCase(repo)

    @Provides
    @Singleton
    fun provideGetSparkAskResultUseCase(repo: com.example.testapp.domain.repository.QuestionAskRepository): GetSparkAskResultUseCase =
        GetSparkAskResultUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveSparkAskResultUseCase(repo: com.example.testapp.domain.repository.QuestionAskRepository): SaveSparkAskResultUseCase =
        SaveSparkAskResultUseCase(repo)

    @Provides
    @Singleton
    fun provideGetSparkAnalysisUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): GetSparkAnalysisUseCase =
        GetSparkAnalysisUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveSparkAnalysisUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): SaveSparkAnalysisUseCase =
        SaveSparkAnalysisUseCase(repo)

    @Provides
    @Singleton
    fun provideGetQuestionAnalysisUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): GetQuestionAnalysisUseCase =
        GetQuestionAnalysisUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveQuestionAnalysisUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): SaveQuestionAnalysisUseCase =
        SaveQuestionAnalysisUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveQuestionAnalysisByQuestionIdUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): RemoveQuestionAnalysisByQuestionIdUseCase =
        RemoveQuestionAnalysisByQuestionIdUseCase(repo)

    @Provides
    @Singleton
    fun provideGetQuestionNoteUseCase(repo: com.example.testapp.domain.repository.QuestionNoteRepository): GetQuestionNoteUseCase =
        GetQuestionNoteUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveQuestionNoteUseCase(repo: com.example.testapp.domain.repository.QuestionNoteRepository): SaveQuestionNoteUseCase =
        SaveQuestionNoteUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveQuestionNoteByQuestionIdUseCase(repo: com.example.testapp.domain.repository.QuestionNoteRepository): RemoveQuestionNoteByQuestionIdUseCase =
        RemoveQuestionNoteByQuestionIdUseCase(repo)

    @Provides
    @Singleton
    fun provideGetBaiduAskResultUseCase(repo: com.example.testapp.domain.repository.QuestionAskRepository): com.example.testapp.domain.usecase.GetBaiduAskResultUseCase =
        com.example.testapp.domain.usecase.GetBaiduAskResultUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveBaiduAskResultUseCase(repo: com.example.testapp.domain.repository.QuestionAskRepository): com.example.testapp.domain.usecase.SaveBaiduAskResultUseCase =
        com.example.testapp.domain.usecase.SaveBaiduAskResultUseCase(repo)

    @Provides
    @Singleton
    fun provideGetBaiduAnalysisUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase =
        com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveBaiduAnalysisUseCase(repo: com.example.testapp.domain.repository.QuestionAnalysisRepository): com.example.testapp.domain.usecase.SaveBaiduAnalysisUseCase =
        com.example.testapp.domain.usecase.SaveBaiduAnalysisUseCase(repo)
}
