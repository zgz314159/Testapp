package com.example.testapp.di

import com.example.testapp.core.common.FontSettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FontSettingsEntryPoint {
    fun fontSettingsRepository(): FontSettingsRepository
}
