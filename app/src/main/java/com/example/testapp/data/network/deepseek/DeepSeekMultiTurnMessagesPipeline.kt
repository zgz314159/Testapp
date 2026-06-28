package com.example.testapp.data.network.deepseek

/**
 * 组装 DeepSeek 多轮 messages（官方：每轮请求携带完整 history）。
 * @see <a href="https://api-docs.deepseek.com/guides/multi_round_chat">Multi-round Conversation</a>
 */
object DeepSeekMultiTurnMessagesPipeline {

    fun build(
        priorTurns: List<DeepSeekChatTurn>,
        nextUserContent: String
    ): List<DeepSeekChatMessage> {
        val messages = mutableListOf(
            DeepSeekChatMessage(role = "system", content = DeepSeekChatConfig.SYSTEM_PROMPT)
        )
        priorTurns.forEach { turn ->
            messages.add(DeepSeekChatMessage(role = "user", content = turn.user))
            messages.add(DeepSeekChatMessage(role = "assistant", content = turn.assistant))
        }
        messages.add(DeepSeekChatMessage(role = "user", content = nextUserContent))
        return messages
    }
}
