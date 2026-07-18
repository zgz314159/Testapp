package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.AiCredentialException
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.repository.AiEntitlementRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 未来付费托管额度后端插口。
 * 本轮未开通：即使 entitlement 将来为 true，也需服务端 access token；当前直接拒绝。
 */
@Singleton
class ManagedAiBackend @Inject constructor(
    private val entitlementRepository: AiEntitlementRepository,
) : AiBackend {
    override suspend fun chat(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean,
        useWebSearch: Boolean,
    ): String {
        ensureReady()
        throw AiCredentialException.ManagedNotAvailable()
    }

    override suspend fun correctQuestion(
        request: QuestionCorrectionRequest,
    ): QuestionCorrectionSuggestion {
        ensureReady()
        throw AiCredentialException.ManagedNotAvailable()
    }

    private suspend fun ensureReady() {
        if (!entitlementRepository.hasManagedAccessNow() ||
            entitlementRepository.getManagedAccessToken().isNullOrBlank()
        ) {
            throw AiCredentialException.ManagedNotAvailable()
        }
    }
}
