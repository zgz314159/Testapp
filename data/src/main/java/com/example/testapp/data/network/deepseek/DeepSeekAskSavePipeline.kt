package com.example.testapp.data.network.deepseek

/** 退出保存：落库用展示文本（多轮答案 `---` 拼接），供答题页直接显示。 */
object DeepSeekAskSavePipeline {

    fun resolvePersistText(turns: List<DeepSeekChatTurn>, displayText: String): String {
        val fromTurns = DeepSeekAskDisplayPipeline.fromTurns(turns).trim()
        if (fromTurns.isNotBlank()) return fromTurns
        return displayText.trim()
    }
}
