package com.example.testapp.domain.repository

import com.example.testapp.domain.model.AiCapability
import com.example.testapp.domain.model.AiCredentialStatus
import kotlinx.coroutines.flow.Flow

interface AiCredentialsRepository {
    fun status(): Flow<AiCredentialStatus>

    suspend fun getDeepSeekApiKey(): String?

    suspend fun getBochaApiKey(): String?

    suspend fun getTavilyApiKey(): String?

    suspend fun setDeepSeekApiKey(key: String)

    suspend fun clearDeepSeekApiKey()

    suspend fun setBochaApiKey(key: String)

    suspend fun clearBochaApiKey()

    suspend fun setTavilyApiKey(key: String)

    suspend fun clearTavilyApiKey()

    /** 校验当前 BYOK 是否满足能力；不足时抛 [com.example.testapp.domain.model.AiCredentialException]。 */
    suspend fun requireUserKeysFor(capability: AiCapability)
}
