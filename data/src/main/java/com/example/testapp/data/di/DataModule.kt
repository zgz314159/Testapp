package com.example.testapp.data.di

import android.content.Context
import androidx.room.Room
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.data.datastore.FontSettingsRepositoryImpl
import com.example.testapp.data.init.QuestionDataInitializer
import com.example.testapp.data.local.AppDatabase
import com.example.testapp.data.local.AppDatabaseMigrations
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.repository.QuestionRepositoryImpl
import com.example.testapp.domain.repository.QuestionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    @Singleton
    abstract fun bindQuestionAnalysisRepository(impl: com.example.testapp.data.repository.QuestionAnalysisRepositoryImpl): com.example.testapp.domain.repository.QuestionAnalysisRepository

    @Binds
    abstract fun bindQuestionAskRepository(impl: com.example.testapp.data.repository.QuestionAskRepositoryImpl): com.example.testapp.domain.repository.QuestionAskRepository

    @Binds
    abstract fun bindExamHistoryRepository(impl: com.example.testapp.data.repository.ExamHistoryRepositoryImpl): com.example.testapp.domain.repository.ExamHistoryRepository

    @Binds
    abstract fun bindQuestionNoteRepository(impl: com.example.testapp.data.repository.QuestionNoteRepositoryImpl): com.example.testapp.domain.repository.QuestionNoteRepository

    @Binds
    abstract fun bindFontSettingsRepository(impl: FontSettingsRepositoryImpl): FontSettingsRepository

    @Binds
    abstract fun bindAdaptiveAtomRepository(
        impl: com.example.testapp.data.repository.AdaptiveAtomRepositoryImpl,
    ): com.example.testapp.domain.repository.AdaptiveAtomRepository

    @Binds
    abstract fun bindQuestionCorrectionRepository(
        impl: com.example.testapp.data.repository.QuestionCorrectionRepositoryImpl,
    ): com.example.testapp.domain.repository.QuestionCorrectionRepository

    @Binds
    @Singleton
    abstract fun bindAiCredentialsRepository(
        impl: com.example.testapp.data.repository.AiCredentialsRepositoryImpl,
    ): com.example.testapp.domain.repository.AiCredentialsRepository

    @Binds
    @Singleton
    abstract fun bindAiEntitlementRepository(
        impl: com.example.testapp.data.repository.AiEntitlementRepositoryImpl,
    ): com.example.testapp.domain.repository.AiEntitlementRepository
}

/**
 * Disk-backed Room binding used by the application.
 * via @TestInstallIn — production behavior unchanged.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
            .addMigrations(AppDatabaseMigrations.migration26To27)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
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

    @Provides
    fun provideAdaptiveAtomStateDao(
        db: AppDatabase,
    ): com.example.testapp.data.local.dao.AdaptiveAtomStateDao = db.adaptiveAtomStateDao()
}
