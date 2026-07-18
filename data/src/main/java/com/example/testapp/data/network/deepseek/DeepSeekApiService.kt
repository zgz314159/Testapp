package com.example.testapp.data.network.deepseek

import com.example.testapp.data.network.ai.AiBackend
import com.example.testapp.domain.model.Question
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeepSeek 入口：默认经 BYOK（用户自备 Key）直连；托管额度走 [AiBackend] 路由。
 */
@Singleton
class DeepSeekApiService @Inject constructor(
    private val aiBackend: AiBackend,
) {
    /**
     * @param enableThinking 标答校对等争议轮次建议 true
     * @param useWebSearch 启用后先经用户配置的博查/Tavily 检索，再交给 DeepSeek 回答
     */
    suspend fun chat(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean = false,
        useWebSearch: Boolean = false,
    ): String = aiBackend.chat(messages, enableThinking, useWebSearch)

    private suspend fun complete(userContent: String): String =
        chat(DeepSeekChatMessages.build(userContent))

    suspend fun analyze(question: Question): String {
        val prompt = DeepSeekExamPromptPipeline.buildAnalyzeUserContent(question)
        return complete(prompt)
    }

    suspend fun ask(text: String): String = complete(text)
}
