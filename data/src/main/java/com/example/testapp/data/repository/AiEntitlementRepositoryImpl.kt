package com.example.testapp.data.repository

import com.example.testapp.domain.repository.AiEntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/** 付费托管额度占位：本轮始终无托管访问。 */
@Singleton
class AiEntitlementRepositoryImpl @Inject constructor() : AiEntitlementRepository {
    override fun hasManagedAccess(): Flow<Boolean> = flowOf(false)

    override suspend fun hasManagedAccessNow(): Boolean = false

    override suspend fun getManagedAccessToken(): String? = null
}
