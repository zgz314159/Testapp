package com.example.testapp.data.network.deepseek

/** 提问页展示：多轮 assistant 按分隔符纵向拼接。 */
object DeepSeekAskDisplayPipeline {

    fun fromTurns(turns: List<DeepSeekChatTurn>): String =
        turns.map { it.assistant.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(DeepSeekAskPersistFormatPipeline.ASSISTANT_SEPARATOR)
}
