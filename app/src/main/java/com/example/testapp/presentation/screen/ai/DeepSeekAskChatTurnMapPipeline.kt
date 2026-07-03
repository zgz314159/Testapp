package com.example.testapp.presentation.screen.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatTurn
import com.example.testapp.uicommon.model.AiChatTurn

/** DeepSeek 多轮 → ui-common 展示模型。 */
object DeepSeekAskChatTurnMapPipeline {

    fun map(turns: List<DeepSeekChatTurn>): List<AiChatTurn> =
        turns.map { AiChatTurn(user = it.user, assistant = it.assistant) }
}
