package com.example.testapp.di

import android.content.Context
import androidx.room.Room
import com.example.testapp.data.local.AppDatabase
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.dao.HistoryRecordDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.data.local.dao.PracticeProgressDao
import com.example.testapp.data.local.dao.ExamProgressDao
import com.example.testapp.data.repository.FavoriteQuestionRepositoryImpl
import com.example.testapp.data.repository.HistoryRepositoryImpl
import com.example.testapp.data.repository.QuestionRepositoryImpl
import com.example.testapp.data.repository.WrongBookRepositoryImpl
import com.example.testapp.data.repository.PracticeProgressRepositoryImpl
import com.example.testapp.data.repository.ExamProgressRepositoryImpl
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.repository.PracticeProgressRepository
import com.example.testapp.domain.repository.ExamProgressRepository
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.GetHistoryListUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.domain.usecase.SaveExamProgressUseCase
import com.example.testapp.domain.usecase.GetExamProgressFlowUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideQuestionRepository(
        dao: QuestionDao,
        favoriteDao: FavoriteQuestionDao
    ): QuestionRepository = QuestionRepositoryImpl(dao, favoriteDao)

    @Provides
    @Singleton
    fun provideGetQuestionsUseCase(repository: QuestionRepository): GetQuestionsUseCase =
        GetQuestionsUseCase(repository)

    @Provides
    @Singleton
    fun provideWrongBookRepository(
        wrongDao: WrongQuestionDao,
        questionDao: QuestionDao
    ): WrongBookRepository = WrongBookRepositoryImpl(wrongDao, questionDao)

    @Provides
    @Singleton
    fun provideAddWrongQuestionUseCase(repo: WrongBookRepository): AddWrongQuestionUseCase =
        AddWrongQuestionUseCase(repo)

    @Provides
    @Singleton
    fun provideGetWrongBookUseCase(repo: WrongBookRepository): GetWrongBookUseCase =
        GetWrongBookUseCase(repo)

    @Provides
    @Singleton
    fun provideRemoveWrongQuestionsByFileNameUseCase(repo: WrongBookRepository): RemoveWrongQuestionsByFileNameUseCase =
        RemoveWrongQuestionsByFileNameUseCase(repo)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        historyDao: HistoryRecordDao,
        questionDao: QuestionDao
    ): HistoryRepository = HistoryRepositoryImpl(historyDao, questionDao)

    @Provides
    @Singleton
    fun provideAddHistoryRecordUseCase(repo: HistoryRepository): AddHistoryRecordUseCase =
        AddHistoryRecordUseCase(repo)

    @Provides
    @Singleton
    fun provideGetHistoryListUseCase(repo: HistoryRepository): GetHistoryListUseCase =
        GetHistoryListUseCase(repo)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "quiz_db")
            .fallbackToDestructiveMigration() // 允许破坏性迁移，开发环境推荐
            .build()

    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideWrongQuestionDao(db: AppDatabase): WrongQuestionDao = db.wrongQuestionDao()

    @Provides
    fun provideHistoryRecordDao(db: AppDatabase): HistoryRecordDao = db.historyRecordDao()

    @Provides
    fun provideFavoriteQuestionDao(db: AppDatabase): FavoriteQuestionDao = db.favoriteQuestionDao()

    @Provides
    fun providePracticeProgressDao(db: AppDatabase): PracticeProgressDao = db.practiceProgressDao()

    @Provides
    fun provideExamProgressDao(db: AppDatabase): ExamProgressDao = db.examProgressDao()

    @Provides
    @Singleton
    fun provideFavoriteQuestionRepository(dao: FavoriteQuestionDao): FavoriteQuestionRepository = FavoriteQuestionRepositoryImpl(dao)

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
    fun providePracticeProgressRepository(dao: PracticeProgressDao): PracticeProgressRepository = PracticeProgressRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideExamProgressRepository(dao: ExamProgressDao): ExamProgressRepository = ExamProgressRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSavePracticeProgressUseCase(repo: PracticeProgressRepository): SavePracticeProgressUseCase = SavePracticeProgressUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveExamProgressUseCase(repo: ExamProgressRepository): SaveExamProgressUseCase = SaveExamProgressUseCase(repo)

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

}
