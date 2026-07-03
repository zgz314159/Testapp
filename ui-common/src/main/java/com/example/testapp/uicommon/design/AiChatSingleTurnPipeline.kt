package com.example.testapp.uicommon.design

import com.example.testapp.uicommon.model.AiChatMessage
import com.example.testapp.uicommon.model.AiChatMessageRole

/** 单轮问答页：user + assistant 两条气泡。 */
object AiChatSingleTurnPipeline {

    fun build(user: String, assistant: String): List<AiChatMessage> {
        val trimmedUser = user.trim()
        val trimmedAssistant = assistant.trim()
        if (trimmedUser.isEmpty() && trimmedAssistant.isEmpty()) return emptyList()
        return buildList {
            if (trimmedUser.isNotEmpty()) {
                add(AiChatMessage(AiChatMessageRole.User, trimmedUser))
            }
            if (trimmedAssistant.isNotEmpty()) {
                add(AiChatMessage(AiChatMessageRole.Assistant, trimmedAssistant))
            }
        }
    }
}
