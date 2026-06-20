package com.example.testapp.di

import android.content.Context
import androidx.room.Room
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.data.datastore.FontSettingsRepositoryImpl
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.data.network.spark.SparkApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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
                }
            )
        }
        install(Logging) { level = LogLevel.NONE }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000  // 增加到120秒
            connectTimeoutMillis = 30_000   // 连接超时30秒
            socketTimeoutMillis = 120_000   // Socket超时120秒
        }
    }

    @Provides
    @Singleton
    fun provideDeepSeekApiService(client: HttpClient): DeepSeekApiService = DeepSeekApiService(client)
    @Provides
    @Singleton
    fun provideSparkApiService(client: HttpClient): SparkApiService = SparkApiService(client)

    @Provides
    @Singleton
    fun provideFontSettingsRepository(impl: FontSettingsRepositoryImpl): FontSettingsRepository = impl
}
