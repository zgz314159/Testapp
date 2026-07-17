package com.example.testapp.di

import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.presentation.session.adaptive.AdaptiveFadingProgressExtension
import com.example.testapp.presentation.session.extension.SessionAiAnalysisExtension
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object SessionExtensionModule {
    @Provides
    @IntoSet
    fun provideAiAnalysisExtension(ext: SessionAiAnalysisExtension): SessionExtension = ext

    @Provides
    @IntoSet
    fun provideAdaptiveFadingProgressExtension(
        ext: AdaptiveFadingProgressExtension,
    ): SessionExtension = ext
}
