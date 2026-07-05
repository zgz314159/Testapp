package com.example.testapp.di

import com.example.testapp.domain.usecase.AddFolderUseCase
import com.example.testapp.domain.usecase.DeleteFolderUseCase
import com.example.testapp.domain.usecase.GetFileFoldersUseCase
import com.example.testapp.domain.usecase.GetFoldersUseCase
import com.example.testapp.domain.usecase.MoveFileToFolderUseCase
import com.example.testapp.domain.usecase.RenameFolderUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileUseCaseModule {

    @Provides
    @Singleton
    fun provideMoveFileToFolderUseCase(repo: com.example.testapp.domain.repository.FileFolderRepository): MoveFileToFolderUseCase = MoveFileToFolderUseCase(repo)

    @Provides
    @Singleton
    fun provideGetFileFoldersUseCase(repo: com.example.testapp.domain.repository.FileFolderRepository): GetFileFoldersUseCase = GetFileFoldersUseCase(repo)

    @Provides
    @Singleton
    fun provideGetFoldersUseCase(repo: com.example.testapp.domain.repository.FileFolderRepository): GetFoldersUseCase = GetFoldersUseCase(repo)

    @Provides
    @Singleton
    fun provideAddFolderUseCase(repo: com.example.testapp.domain.repository.FileFolderRepository): AddFolderUseCase = AddFolderUseCase(repo)

    @Provides
    @Singleton
    fun provideRenameFolderUseCase(repo: com.example.testapp.domain.repository.FileFolderRepository): RenameFolderUseCase = RenameFolderUseCase(repo)

    @Provides
    @Singleton
    fun provideDeleteFolderUseCase(repo: com.example.testapp.domain.repository.FileFolderRepository): DeleteFolderUseCase = DeleteFolderUseCase(repo)
}
