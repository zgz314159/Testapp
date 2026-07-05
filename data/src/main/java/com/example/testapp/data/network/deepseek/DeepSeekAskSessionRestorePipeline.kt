package com.example.testapp.data.network.deepseek

/** 打开 DeepSeek 问答页时的首问/会话恢复。 */
object DeepSeekAskSessionRestorePipeline {

    fun firstQuestionText(routeQuestionText: String, examAnchor: DeepSeekExamAnchor?): String {
        routeQuestionText.trim().takeIf { it.isNotBlank() }?.let { return it }
        return examAnchor?.content?.trim().orEmpty().ifBlank { "请解析本题并给出最终答案。" }
    }
}
