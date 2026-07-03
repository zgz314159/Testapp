package com.example.testapp.uicommon.design

/** 无状态：发送按钮是否可点。 */
object AiChatSendEnabledPipeline {

    fun isEnabled(sendEnabled: Boolean, input: String): Boolean =
        sendEnabled && input.trim().isNotEmpty()
}
