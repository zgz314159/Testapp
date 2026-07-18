package com.example.testapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.testapp.data.security.AiCredentialCrypto
import com.example.testapp.domain.model.AiCapability
import com.example.testapp.domain.model.AiCredentialException
import com.example.testapp.domain.model.AiCredentialStatus
import com.example.testapp.domain.repository.AiCredentialsRepository
import com.example.testapp.domain.repository.AiEntitlementRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiCredentialsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val entitlementRepository: AiEntitlementRepository,
) : AiCredentialsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private data class KeySlot(val secretKey: String, val hintKey: String) {
        fun configuredIn(prefs: SharedPreferences): Boolean =
            !prefs.getString(secretKey, null).isNullOrBlank()

        fun hintIn(prefs: SharedPreferences): String =
            prefs.getString(hintKey, "").orEmpty()
    }

    private val deepSeekSlot = KeySlot(KEY_DEEPSEEK, KEY_DEEPSEEK_HINT)
    private val bochaSlot = KeySlot(KEY_BOCHA, KEY_BOCHA_HINT)
    private val tavilySlot = KeySlot(KEY_TAVILY, KEY_TAVILY_HINT)

    private val slotsState = MutableStateFlow(readSlotsSnapshot())

    override fun status(): Flow<AiCredentialStatus> = combine(
        slotsState,
        entitlementRepository.hasManagedAccess(),
    ) { slots, managed ->
        slots.copy(managedAccessAvailable = managed)
    }

    override suspend fun getDeepSeekApiKey(): String? = readSecret(deepSeekSlot)

    override suspend fun getBochaApiKey(): String? = readSecret(bochaSlot)

    override suspend fun getTavilyApiKey(): String? = readSecret(tavilySlot)

    override suspend fun setDeepSeekApiKey(key: String) = writeSecret(deepSeekSlot, key)

    override suspend fun clearDeepSeekApiKey() = clearSecret(deepSeekSlot)

    override suspend fun setBochaApiKey(key: String) = writeSecret(bochaSlot, key)

    override suspend fun clearBochaApiKey() = clearSecret(bochaSlot)

    override suspend fun setTavilyApiKey(key: String) = writeSecret(tavilySlot, key)

    override suspend fun clearTavilyApiKey() = clearSecret(tavilySlot)

    override suspend fun requireUserKeysFor(capability: AiCapability) {
        if (getDeepSeekApiKey().isNullOrBlank()) {
            throw AiCredentialException.MissingDeepSeekKey()
        }
        if (capability != AiCapability.CHAT &&
            getBochaApiKey().isNullOrBlank() &&
            getTavilyApiKey().isNullOrBlank()
        ) {
            throw AiCredentialException.MissingSearchKey()
        }
    }

    private fun readSlotsSnapshot(): AiCredentialStatus = AiCredentialStatus(
        deepSeekConfigured = deepSeekSlot.configuredIn(prefs),
        bochaConfigured = bochaSlot.configuredIn(prefs),
        tavilyConfigured = tavilySlot.configuredIn(prefs),
        deepSeekHint = deepSeekSlot.hintIn(prefs),
        bochaHint = bochaSlot.hintIn(prefs),
        tavilyHint = tavilySlot.hintIn(prefs),
    )

    private fun readSecret(slot: KeySlot): String? {
        val blob = prefs.getString(slot.secretKey, null) ?: return null
        return runCatching { AiCredentialCrypto.decrypt(blob).trim() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private fun writeSecret(slot: KeySlot, plain: String) {
        val trimmed = plain.trim()
        require(trimmed.isNotBlank()) { "API Key 不能为空" }
        prefs.edit()
            .putString(slot.secretKey, AiCredentialCrypto.encrypt(trimmed))
            .putString(slot.hintKey, maskHint(trimmed))
            .apply()
        slotsState.value = readSlotsSnapshot()
    }

    private fun clearSecret(slot: KeySlot) {
        prefs.edit().remove(slot.secretKey).remove(slot.hintKey).apply()
        slotsState.value = readSlotsSnapshot()
    }

    private fun maskHint(key: String): String {
        val trimmed = key.trim()
        if (trimmed.length <= 4) return "••••"
        return "••••${trimmed.takeLast(4)}"
    }

    private companion object {
        const val PREFS_NAME = "ai_credentials_encrypted"
        const val KEY_DEEPSEEK = "deepseek_api_key_enc"
        const val KEY_BOCHA = "bocha_api_key_enc"
        const val KEY_TAVILY = "tavily_api_key_enc"
        const val KEY_DEEPSEEK_HINT = "deepseek_api_key_hint"
        const val KEY_BOCHA_HINT = "bocha_api_key_hint"
        const val KEY_TAVILY_HINT = "tavily_api_key_hint"
    }
}
