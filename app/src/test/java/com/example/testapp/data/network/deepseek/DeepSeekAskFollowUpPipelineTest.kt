package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekAskFollowUpPipelineTest {

    private val anchor = DeepSeekExamAnchor(
        questionType = "多选题",
        content = "题干",
        options = listOf("A", "B"),
        standardAnswer = "ABCDE",
        officialExplanation = "解析"
    )

    @Test
    fun firstTurn_wrapsUserQuestion() {
        val result = DeepSeekAskFollowUpPipeline.resolveNextUserContent(
            firstQuestion = "",
            currentQuestionInput = "为什么选ABCDE",
            isFollowUp = false,
            examAnchor = anchor
        )
        assertTrue(result!!.contains("【用户提问】"))
        assertTrue(result.contains("为什么选ABCDE"))
    }

    @Test
    fun followUp_doesNotAutoInjectRevisionPrompt() {
        assertNull(
            DeepSeekAskFollowUpPipeline.resolveNextUserContent(
                firstQuestion = "题干",
                currentQuestionInput = "",
                isFollowUp = true,
                examAnchor = anchor
            )
        )
    }

    @Test
    fun challengeOnly_wrapsWithStabilityInstruction() {
        val result = DeepSeekAskFollowUpPipeline.resolveNextUserContent(
            firstQuestion = "题干",
            currentQuestionInput = "你确定吗",
            isFollowUp = true,
            examAnchor = anchor
        )
        assertTrue(result!!.contains("未提供新的题干"))
        assertTrue(result.contains("你确定吗"))
    }

    @Test
    fun normalFollowUp_passesUserText() {
        val result = DeepSeekAskFollowUpPipeline.resolveNextUserContent(
            firstQuestion = "题干",
            currentQuestionInput = "请解释C选项为何不对",
            isFollowUp = true,
            examAnchor = anchor
        )
        assertTrue(result!!.contains("请解释C选项为何不对"))
    }
}
