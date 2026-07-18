package com.example.testapp.data.di

import com.example.testapp.data.network.ai.AiBackend
import com.example.testapp.data.network.ai.ManagedAiBackend
import com.example.testapp.data.network.ai.RoutingAiBackend
import com.example.testapp.data.network.ai.UserKeysAiBackend
import com.example.testapp.data.network.baidu.BaiduApiService
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.data.network.spark.SparkApiService
import com.example.testapp.domain.repository.AiCredentialsRepository
import com.example.testapp.domain.repository.AiEntitlementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        engine {
            requestTimeout = 60_000
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
            )
        }
        install(Logging) { level = LogLevel.NONE }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 120_000
        }
    }

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

    @Provides
    @Singleton
    fun provideDeepSeekApiService(aiBackend: AiBackend): DeepSeekApiService =
        DeepSeekApiService(aiBackend)

    @Provides
    @Singleton
    fun provideSparkApiService(client: HttpClient): SparkApiService = SparkApiService(client)

    @Provides
    @Singleton
    fun provideBaiduApiService(client: HttpClient): BaiduApiService = BaiduApiService(client)
}
