package com.example.testapp.data.di

import com.example.testapp.data.network.ai.AiBackend
import com.example.testapp.data.network.ai.ManagedAiBackend
import com.example.testapp.data.network.ai.RoutingAiBackend
import com.example.testapp.data.network.ai.UserKeysAiBackend
import com.example.testapp.domain.repository.AiCredentialsRepository
import com.example.testapp.domain.repository.AiEntitlementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Production AiBackend binding.
 * via @TestInstallIn — no real network/paid API calls in instrumented tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiBackendModule {
    @Provides
    @Singleton
    fun provideAiBackend(
        credentialsRepository: AiCredentialsRepository,
        entitlementRepository: AiEntitlementRepository,
        userKeysAiBackend: UserKeysAiBackend,
        managedAiBackend: ManagedAiBackend,
    ): AiBackend = RoutingAiBackend(
        credentialsRepository = credentialsRepository,
        entitlementRepository = entitlementRepository,
        userKeysAiBackend = userKeysAiBackend,
        managedAiBackend = managedAiBackend,
    )
}
