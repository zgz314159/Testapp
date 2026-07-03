package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Test

class DeepSeekAskSessionRestorePipelineTest {

    private val anchor = DeepSeekExamAnchor(
        questionType = "单选题",
        content = "题干内容",
        options = listOf("A", "B"),
        standardAnswer = "A",
        officialExplanation = ""
    )

    @Test
    fun firstQuestionText_prefersRouteText() {
        assertEquals("用户选中文字", DeepSeekAskSessionRestorePipeline.firstQuestionText("用户选中文字", anchor))
    }

    @Test
    fun firstQuestionText_fallsBackToAnchor() {
        assertEquals("题干内容", DeepSeekAskSessionRestorePipeline.firstQuestionText("", anchor))
    }
}
