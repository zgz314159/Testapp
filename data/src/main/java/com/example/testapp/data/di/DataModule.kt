package com.example.testapp.data.di

import android.content.Context
import androidx.room.Room
import com.example.testapp.data.local.AppDatabase
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.repository.QuestionRepositoryImpl
import com.example.testapp.domain.repository.QuestionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import com.example.testapp.data.init.QuestionDataInitializer

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindModule {
    @Binds
    abstract fun bindQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository
    @Binds
    abstract fun bindFileStatisticsRepository(impl: com.example.testapp.data.repository.FileStatisticsRepositoryImpl): com.example.testapp.domain.repository.FileStatisticsRepository
    @Binds
    abstract fun bindWrongBookRepository(impl: com.example.testapp.data.repository.WrongBookRepositoryImpl): com.example.testapp.domain.repository.WrongBookRepository
    @Binds
    abstract fun bindHistoryRepository(impl: com.example.testapp.data.repository.HistoryRepositoryImpl): com.example.testapp.domain.repository.HistoryRepository
    @Binds
    abstract fun bindExamProgressRepository(impl: com.example.testapp.data.repository.ExamProgressRepositoryImpl): com.example.testapp.domain.repository.ExamProgressRepository
    @Binds
    abstract fun bindFavoriteQuestionRepository(impl: com.example.testapp.data.repository.FavoriteQuestionRepositoryImpl): com.example.testapp.domain.repository.FavoriteQuestionRepository
    @Binds
    abstract fun bindPracticeProgressRepository(impl: com.example.testapp.data.repository.PracticeProgressRepositoryImpl): com.example.testapp.domain.repository.PracticeProgressRepository
    @Binds
    abstract fun bindFileFolderRepository(impl: com.example.testapp.data.repository.FileFolderRepositoryImpl): com.example.testapp.domain.repository.FileFolderRepository
    @Binds
    abstract fun bindQuestionAnalysisRepository(impl: com.example.testapp.data.repository.QuestionAnalysisRepositoryImpl): com.example.testapp.domain.repository.QuestionAnalysisRepository
    @Binds
    abstract fun bindQuestionAskRepository(impl: com.example.testapp.data.repository.QuestionAskRepositoryImpl): com.example.testapp.domain.repository.QuestionAskRepository
    @Binds
    abstract fun bindExamHistoryRepository(impl: com.example.testapp.data.repository.ExamHistoryRepositoryImpl): com.example.testapp.domain.repository.ExamHistoryRepository
    @Binds
    abstract fun bindQuestionNoteRepository(impl: com.example.testapp.data.repository.QuestionNoteRepositoryImpl): com.example.testapp.domain.repository.QuestionNoteRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideQuestionDataInitializer(
        @ApplicationContext context: Context
    ): QuestionDataInitializer {
        return QuestionDataInitializer(context)
    }

    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()
    @Provides
    fun provideFavoriteQuestionDao(db: AppDatabase): FavoriteQuestionDao = db.favoriteQuestionDao()

    @Provides
    fun provideWrongQuestionDao(db: AppDatabase): com.example.testapp.data.local.dao.WrongQuestionDao = db.wrongQuestionDao()

    @Provides
    fun provideHistoryRecordDao(db: AppDatabase): com.example.testapp.data.local.dao.HistoryRecordDao = db.historyRecordDao()

    @Provides
    fun providePracticeProgressDao(db: AppDatabase): com.example.testapp.data.local.dao.PracticeProgressDao = db.practiceProgressDao()

    @Provides
    fun provideExamProgressDao(db: AppDatabase): com.example.testapp.data.local.dao.ExamProgressDao = db.examProgressDao()

    @Provides
    fun provideQuestionAnalysisDao(db: AppDatabase): com.example.testapp.data.local.dao.QuestionAnalysisDao = db.questionAnalysisDao()

    @Provides
    fun provideQuestionNoteDao(db: AppDatabase): com.example.testapp.data.local.dao.QuestionNoteDao = db.questionNoteDao()

    @Provides
    fun provideQuestionAskDao(db: AppDatabase): com.example.testapp.data.local.dao.QuestionAskDao = db.questionAskDao()

    @Provides
    fun provideExamHistoryDao(db: AppDatabase): com.example.testapp.data.local.dao.ExamHistoryRecordDao = db.examHistoryRecordDao()

    @Provides
    fun provideFileFolderDao(db: AppDatabase): com.example.testapp.data.local.dao.FileFolderDao = db.fileFolderDao()

    @Provides
    fun provideFolderDao(db: AppDatabase): com.example.testapp.data.local.dao.FolderDao = db.folderDao()
}
