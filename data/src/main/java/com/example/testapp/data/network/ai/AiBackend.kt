package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion

interface AiBackend {
    suspend fun chat(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean = false,
        useWebSearch: Boolean = false,
    ): String

    suspend fun correctQuestion(request: QuestionCorrectionRequest): QuestionCorrectionSuggestion
}
