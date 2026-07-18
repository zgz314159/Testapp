package com.example.testapp.domain.model

/** AI 调用能力：普通对话只需 DeepSeek；联网纠题还需检索 Key（博查或 Tavily 之一）。 */
enum class AiCapability {
    CHAT,
    CHAT_ONLINE,
    CORRECT_ONLINE,
}

/** 当前 AI 提供方。USER_KEYS = BYOK；MANAGED = 未来付费托管额度。 */
enum class AiProviderKind {
    USER_KEYS,
    MANAGED,
}

/** 联网检索提供方；都配置时优先博查（大陆直连稳定、中文质量更好）。 */
enum class SearchProviderKind {
    BOCHA,
    TAVILY,
}

data class AiCredentialStatus(
    val deepSeekConfigured: Boolean = false,
    val bochaConfigured: Boolean = false,
    val tavilyConfigured: Boolean = false,
    /** 已配置时展示掩码尾号，如 `••••abcd`；未配置为空。 */
    val deepSeekHint: String = "",
    val bochaHint: String = "",
    val tavilyHint: String = "",
    val managedAccessAvailable: Boolean = false,
) {
    val searchConfigured: Boolean get() = bochaConfigured || tavilyConfigured

    fun readyFor(capability: AiCapability): Boolean = when (capability) {
        AiCapability.CHAT -> deepSeekConfigured || managedAccessAvailable
        AiCapability.CHAT_ONLINE,
        AiCapability.CORRECT_ONLINE ->
            (deepSeekConfigured && searchConfigured) || managedAccessAvailable
    }
}

sealed class AiCredentialException(message: String) : IllegalStateException(message) {
    class MissingDeepSeekKey : AiCredentialException(
        "未配置 DeepSeek API Key，请到设置 → AI 服务中填写",
    )

    class MissingSearchKey : AiCredentialException(
        "未配置联网检索 Key，联网纠题需要博查或 Tavily 之一，请到设置 → AI 服务中填写",
    )

    class ManagedNotAvailable : AiCredentialException(
        "托管额度尚未开放，请先填写自己的 API Key，或等待购买功能上线",
    )
}
