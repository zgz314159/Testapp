package com.example.testapp.di

import com.example.testapp.data.network.baidu.BaiduApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideBaiduApiService(client: HttpClient): BaiduApiService {
        return BaiduApiService(client)
    }
}

