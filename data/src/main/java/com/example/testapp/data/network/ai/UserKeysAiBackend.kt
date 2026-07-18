package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.AiCapability
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.repository.AiCredentialsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserKeysAiBackend @Inject constructor(
    private val credentialsRepository: AiCredentialsRepository,
    private val deepSeekDirectClient: DeepSeekDirectClient,
    private val webSearchOrchestrator: AiWebSearchOrchestrator,
    private val correctionOrchestrator: QuestionCorrectionOrchestrator,
) : AiBackend {
    override suspend fun chat(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean,
        useWebSearch: Boolean,
    ): String {
        val capability = if (useWebSearch) AiCapability.CHAT_ONLINE else AiCapability.CHAT
        credentialsRepository.requireUserKeysFor(capability)
        val key = credentialsRepository.getDeepSeekApiKey().orEmpty()
        if (!useWebSearch) {
            return deepSeekDirectClient.chat(key, messages, enableThinking)
        }
        val query = AiWebSearchPromptPipeline.latestUserQuery(messages)
        val sources = webSearchOrchestrator.search(query)
        val requestMessages = AiWebSearchPromptPipeline.attachSources(messages, sources)
        val response = deepSeekDirectClient.chat(key, requestMessages, enableThinking)
        return AiWebSearchPromptPipeline.appendCitations(response, sources)
    }

    override suspend fun correctQuestion(
        request: QuestionCorrectionRequest,
    ): QuestionCorrectionSuggestion {
        credentialsRepository.requireUserKeysFor(AiCapability.CORRECT_ONLINE)
        val deepSeekKey = credentialsRepository.getDeepSeekApiKey().orEmpty()
        return correctionOrchestrator.correct(deepSeekKey, request)
    }
}
