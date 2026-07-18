package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.AiCapability
import com.example.testapp.domain.model.AiCredentialException
import com.example.testapp.domain.model.AiCredentialStatus
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.repository.AiCredentialsRepository
import com.example.testapp.domain.repository.AiEntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingAiBackendTest {

    @Test
    fun chat_usesUserKeysWhenDeepSeekConfigured() = runBlocking {
        val user = RecordingBackend("user")
        val managed = RecordingBackend("managed")
        val routing = RoutingAiBackend(
            credentialsRepository = FakeCredentials(deepSeek = "sk-user"),
            entitlementRepository = FakeEntitlement(false),
            userKeysAiBackend = user,
            managedAiBackend = managed,
        )
        assertEquals("user", routing.chat(listOf(DeepSeekChatMessage("user", "hi"))))
        assertEquals(1, user.chatCalls)
        assertEquals(0, managed.chatCalls)
    }

    @Test
    fun chat_throwsMissingDeepSeekWhenNoKeysAndNoManaged() = runBlocking {
        val routing = RoutingAiBackend(
            credentialsRepository = FakeCredentials(),
            entitlementRepository = FakeEntitlement(false),
            userKeysAiBackend = RecordingBackend("user"),
            managedAiBackend = RecordingBackend("managed"),
        )
        val error = runCatching {
            routing.chat(listOf(DeepSeekChatMessage("user", "hi")))
        }.exceptionOrNull()
        assertTrue(error is AiCredentialException.MissingDeepSeekKey)
    }

    @Test
    fun onlineChat_requiresSearchKey() = runBlocking {
        val routing = RoutingAiBackend(
            credentialsRepository = FakeCredentials(deepSeek = "sk-user"),
            entitlementRepository = FakeEntitlement(false),
            userKeysAiBackend = RecordingBackend("user"),
            managedAiBackend = RecordingBackend("managed"),
        )
        val error = runCatching {
            routing.chat(
                messages = listOf(DeepSeekChatMessage("user", "latest news")),
                useWebSearch = true,
            )
        }.exceptionOrNull()
        assertTrue(error is AiCredentialException.MissingSearchKey)
    }

    @Test
    fun correct_throwsMissingSearchWhenOnlyDeepSeek() = runBlocking {
        val routing = RoutingAiBackend(
            credentialsRepository = FakeCredentials(deepSeek = "sk"),
            entitlementRepository = FakeEntitlement(false),
            userKeysAiBackend = RecordingBackend("user"),
            managedAiBackend = RecordingBackend("managed"),
        )
        val error = runCatching {
            routing.correctQuestion(
                QuestionCorrectionRequest(
                    questionType = "单选题",
                    content = "题干",
                    options = listOf("a", "b"),
                    answer = "A",
                ),
            )
        }.exceptionOrNull()
        assertTrue(error is AiCredentialException.MissingSearchKey)
    }

    @Test
    fun correct_usesUserKeysWithBochaOnly() = runBlocking {
        val user = RecordingBackend("user")
        val routing = RoutingAiBackend(
            credentialsRepository = FakeCredentials(deepSeek = "sk", bocha = "bocha-key"),
            entitlementRepository = FakeEntitlement(false),
            userKeysAiBackend = user,
            managedAiBackend = RecordingBackend("managed"),
        )
        val suggestion = routing.correctQuestion(
            QuestionCorrectionRequest(
                questionType = "单选题",
                content = "题干",
                options = listOf("a", "b"),
                answer = "A",
            ),
        )
        assertEquals("user", suggestion.content)
    }

    private class RecordingBackend(private val tag: String) : AiBackend {
        var chatCalls = 0
        override suspend fun chat(
            messages: List<DeepSeekChatMessage>,
            enableThinking: Boolean,
            useWebSearch: Boolean,
        ): String {
            chatCalls++
            return tag
        }

        override suspend fun correctQuestion(
            request: QuestionCorrectionRequest,
        ): QuestionCorrectionSuggestion = QuestionCorrectionSuggestion(
            content = tag,
            answer = "A",
            confidence = 0.5,
        )
    }

    private class FakeCredentials(
        private val deepSeek: String? = null,
        private val bocha: String? = null,
        private val tavily: String? = null,
    ) : AiCredentialsRepository {
        override fun status(): Flow<AiCredentialStatus> = flowOf(
            AiCredentialStatus(
                deepSeekConfigured = !deepSeek.isNullOrBlank(),
                bochaConfigured = !bocha.isNullOrBlank(),
                tavilyConfigured = !tavily.isNullOrBlank(),
            ),
        )

        override suspend fun getDeepSeekApiKey(): String? = deepSeek
        override suspend fun getBochaApiKey(): String? = bocha
        override suspend fun getTavilyApiKey(): String? = tavily
        override suspend fun setDeepSeekApiKey(key: String) = Unit
        override suspend fun clearDeepSeekApiKey() = Unit
        override suspend fun setBochaApiKey(key: String) = Unit
        override suspend fun clearBochaApiKey() = Unit
        override suspend fun setTavilyApiKey(key: String) = Unit
        override suspend fun clearTavilyApiKey() = Unit
        override suspend fun requireUserKeysFor(capability: AiCapability) {
            when (capability) {
                AiCapability.CHAT ->
                    if (deepSeek.isNullOrBlank()) throw AiCredentialException.MissingDeepSeekKey()
                AiCapability.CHAT_ONLINE,
                AiCapability.CORRECT_ONLINE -> {
                    if (deepSeek.isNullOrBlank()) throw AiCredentialException.MissingDeepSeekKey()
                    if (bocha.isNullOrBlank() && tavily.isNullOrBlank()) {
                        throw AiCredentialException.MissingSearchKey()
                    }
                }
            }
        }
    }

    private class FakeEntitlement(private val managed: Boolean) : AiEntitlementRepository {
        override fun hasManagedAccess(): Flow<Boolean> = flowOf(managed)
        override suspend fun hasManagedAccessNow(): Boolean = managed
        override suspend fun getManagedAccessToken(): String? = if (managed) "token" else null
    }
}
