package com.example.testapp.uicommon.design

import com.example.testapp.uicommon.model.AiChatMessageRole

/** 无状态：Gemini 风格消息布局参数。 */
object AiChatBubbleLayoutPipeline {

    fun assistantUsesBubble(role: AiChatMessageRole, geminiStyle: Boolean): Boolean =
        !(geminiStyle && role == AiChatMessageRole.Assistant)

    fun userMaxWidthFraction(geminiStyle: Boolean): Float =
        if (geminiStyle) 0.82f else 0.88f
}
