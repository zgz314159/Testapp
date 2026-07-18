package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.AiCapability
import com.example.testapp.domain.model.AiCredentialException
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.repository.AiCredentialsRepository
import com.example.testapp.domain.repository.AiEntitlementRepository

/**
 * 优先 BYOK；无用户 Key 且有托管 entitlement 时走 Managed；否则抛出可识别缺 Key 错误。
 */
class RoutingAiBackend(
    private val credentialsRepository: AiCredentialsRepository,
    private val entitlementRepository: AiEntitlementRepository,
    private val userKeysAiBackend: AiBackend,
    private val managedAiBackend: AiBackend,
) : AiBackend {
    override suspend fun chat(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean,
        useWebSearch: Boolean,
    ): String = route(if (useWebSearch) AiCapability.CHAT_ONLINE else AiCapability.CHAT) {
        it.chat(messages, enableThinking, useWebSearch)
    }

    override suspend fun correctQuestion(
        request: QuestionCorrectionRequest,
    ): QuestionCorrectionSuggestion = route(AiCapability.CORRECT_ONLINE) {
        it.correctQuestion(request)
    }

    private suspend fun <T> route(capability: AiCapability, block: suspend (AiBackend) -> T): T {
        val hasUserKeys = runCatching {
            credentialsRepository.requireUserKeysFor(capability)
            true
        }.getOrDefault(false)
        if (hasUserKeys) return block(userKeysAiBackend)
        if (entitlementRepository.hasManagedAccessNow()) return block(managedAiBackend)
        credentialsRepository.requireUserKeysFor(capability)
        throw AiCredentialException.ManagedNotAvailable()
    }
}
