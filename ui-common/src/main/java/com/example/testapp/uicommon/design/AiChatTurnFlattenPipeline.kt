package com.example.testapp.uicommon.design

import com.example.testapp.uicommon.model.AiChatMessage
import com.example.testapp.uicommon.model.AiChatMessageRole
import com.example.testapp.uicommon.model.AiChatTurn

/** 多轮对话 → 按时间序的聊天气泡列表。 */
object AiChatTurnFlattenPipeline {

    fun flatten(turns: List<AiChatTurn>): List<AiChatMessage> =
        turns.flatMap { turn ->
            buildList {
                turn.user.trim().takeIf { it.isNotEmpty() }?.let {
                    add(AiChatMessage(AiChatMessageRole.User, it))
                }
                turn.assistant.trim().takeIf { it.isNotEmpty() }?.let {
                    add(AiChatMessage(AiChatMessageRole.Assistant, it))
                }
            }
        }
}
