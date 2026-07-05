package com.example.testapp.di

import com.example.testapp.presentation.screen.shared.SessionAnalysisLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SessionAnalysisLoaderEntryPoint {
    fun sessionAnalysisLoader(): SessionAnalysisLoader
}
