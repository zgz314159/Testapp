package com.example.testapp.data.network.deepseek

/**
 * 组装 DeepSeek 多轮 messages（官方：每轮请求携带完整 history）。
 * 考试场景：稳定 system + 题目锚点 + 裁剪历史轮次。
 */
object DeepSeekMultiTurnMessagesPipeline {

    fun build(
        priorTurns: List<DeepSeekChatTurn>,
        nextUserContent: String,
        examAnchor: DeepSeekExamAnchor? = null
    ): List<DeepSeekChatMessage> {
        val messages = mutableListOf(
            DeepSeekChatMessage(
                role = "system",
                content = DeepSeekExamPromptPipeline.systemPrompt(examAnchor)
            )
        )
        DeepSeekChatHistoryPipeline.trimRetainedTurns(priorTurns).forEach { turn ->
            messages.add(DeepSeekChatMessage(role = "user", content = turn.user))
            messages.add(DeepSeekChatMessage(role = "assistant", content = turn.assistant))
        }
        messages.add(DeepSeekChatMessage(role = "user", content = nextUserContent))
        return messages
    }
}
