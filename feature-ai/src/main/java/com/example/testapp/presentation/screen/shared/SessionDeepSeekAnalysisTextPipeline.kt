package com.example.testapp.presentation.screen.shared

import com.example.testapp.data.network.deepseek.DeepSeekAskLoadSeedPipeline
import com.example.testapp.data.network.deepseek.SessionAnalysisInlineDisplayPipeline

/** feature-exam / 非 data 模块访问 DeepSeek 持久化合并与展示。 */
object SessionDeepSeekAnalysisTextPipeline {
    fun toDisplayText(persistedOrDisplay: String, questionStem: String = ""): String =
        SessionAnalysisInlineDisplayPipeline.toDisplayText(persistedOrDisplay, questionStem)

    fun preferStructured(existing: String?, incoming: String?): String =
        DeepSeekAskLoadSeedPipeline.resolvePreferStructured(existing, incoming)
}
