package com.example.testapp.di

import com.example.testapp.core.session.SessionAnalysisLoader
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.session.SessionMemoryMode
import com.example.testapp.core.session.SessionProgressManager
import com.example.testapp.data.session.SessionAnalysisLoaderImpl
import com.example.testapp.data.session.SessionMemoryModeImpl
import com.example.testapp.data.session.SessionProgressManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionProgressManager(impl: SessionProgressManagerImpl): SessionProgressManager = impl

    @Provides
    @Singleton
    fun provideSessionAnalysisLoader(impl: SessionAnalysisLoaderImpl): SessionAnalysisLoader = impl

    @Provides
    @Singleton
    fun provideSessionMemoryMode(impl: SessionMemoryModeImpl): SessionMemoryMode = impl

    @Provides
    @Singleton
    fun provideSessionEngine(
        progressManager: SessionProgressManager,
        analysisLoader: SessionAnalysisLoader,
        memoryMode: SessionMemoryMode
    ): SessionEngine = SessionEngine(progressManager, analysisLoader, memoryMode)
}
