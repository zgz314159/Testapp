package com.example.testapp.di

import com.example.testapp.domain.session.SessionExtension
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SessionExtensionsEntryPoint {
    fun sessionExtensions(): Set<@JvmSuppressWildcards SessionExtension>
}
