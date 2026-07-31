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
    suspend fun search(
        query: String,
        maxResults: Int = 5,
        bochaInclude: String = "",
        tavilyIncludeDomains: List<String> = emptyList(),
    ): List<QuestionCorrectionSource> {
        credentialsRepository.getBochaApiKey()?.takeIf { it.isNotBlank() }?.let { key ->
            return bochaDirectClient.search(
                apiKey = key,
                query = query,
                maxResults = maxResults,
                include = bochaInclude,
            )
        }
        credentialsRepository.getTavilyApiKey()?.takeIf { it.isNotBlank() }?.let { key ->
            return tavilyDirectClient.search(
                apiKey = key,
                query = query,
                maxResults = maxResults,
                includeDomains = tavilyIncludeDomains,
            )
        }
        throw AiCredentialException.MissingSearchKey()
    }

    suspend fun searchSpec(
        spec: QuestionCorrectionSearchQueryPipeline.Spec,
        maxResults: Int = 8,
    ): List<QuestionCorrectionSource> =
        search(
            query = spec.query,
            maxResults = maxResults,
            bochaInclude = spec.bochaInclude,
            tavilyIncludeDomains = spec.tavilyIncludeDomains,
        )
}
