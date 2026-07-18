package com.example.testapp.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 未来付费托管额度插口。
 * 本轮始终返回 false；支付/账户上线后在此接入短期 access token。
 */
interface AiEntitlementRepository {
    fun hasManagedAccess(): Flow<Boolean>

    suspend fun hasManagedAccessNow(): Boolean

    /** 预留：支付成功后由服务端下发的短期托管访问令牌。 */
    suspend fun getManagedAccessToken(): String?
}
