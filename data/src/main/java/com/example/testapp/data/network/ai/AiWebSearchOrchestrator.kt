package com.example.testapp.data.network.ai

import com.example.testapp.domain.model.AiCredentialException
import com.example.testapp.domain.model.QuestionCorrectionSource
import com.example.testapp.domain.repository.AiCredentialsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiWebSearchOrchestrator @Inject constructor(
    private val credentialsRepository: AiCredentialsRepository,
    private val bochaDirectClient: BochaDirectClient,
    private val tavilyDirectClient: TavilyDirectClient,
) {
    suspend fun search(query: String, maxResults: Int = 5): List<QuestionCorrectionSource> {
        credentialsRepository.getBochaApiKey()?.takeIf { it.isNotBlank() }?.let { key ->
            return bochaDirectClient.search(key, query, maxResults)
        }
        credentialsRepository.getTavilyApiKey()?.takeIf { it.isNotBlank() }?.let { key ->
            return tavilyDirectClient.search(key, query, maxResults)
        }
        throw AiCredentialException.MissingSearchKey()
    }
}
