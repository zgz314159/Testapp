package com.example.testapp.data.network.deepseek

/** 退出保存：DB / 会话均落结构化多轮；答题区展示另走 InlineDisplay。 */
object DeepSeekAskSavePipeline {

    fun resolvePersistText(turns: List<DeepSeekChatTurn>, displayText: String): String {
        if (turns.isNotEmpty()) {
            return DeepSeekAskPersistFormatPipeline.encode(turns).trim()
        }
        return displayText.trim()
    }
}
