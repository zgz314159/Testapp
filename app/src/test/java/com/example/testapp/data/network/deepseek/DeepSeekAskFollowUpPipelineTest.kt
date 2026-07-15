package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekAskFollowUpPipelineTest {

    private val anchor = DeepSeekExamAnchor(
        questionType = "判断题",
        content = "题干",
        options = emptyList(),
        standardAnswer = "对",
        officialExplanation = "解析"
    )

    @Test
    fun firstTurn_wrapsUserQuestion() {
        val result = DeepSeekAskFollowUpPipeline.resolveNextUserContent(
            firstQuestion = "",
            currentQuestionInput = "为什么选对",
            isFollowUp = false,
            examAnchor = anchor
        )
        assertTrue(result!!.contains("【用户提问】"))
        assertTrue(result.contains("为什么选对"))
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
        val resolved = DeepSeekAskFollowUpPipeline.resolve(
            firstQuestion = "题干",
            currentQuestionInput = "你确定吗",
            isFollowUp = true,
            examAnchor = anchor
        )
        assertEquals(DeepSeekAskFollowUpPipeline.FollowUpKind.CHALLENGE_ONLY, resolved.kind)
        assertFalse(resolved.enableThinking)
        assertTrue(resolved.userContent!!.contains("未提供标答"))
        assertTrue(resolved.userContent!!.contains("你确定吗"))
    }

    @Test
    fun answerKeyFeedback_wrapsReviewAndEnablesThinking() {
        val resolved = DeepSeekAskFollowUpPipeline.resolve(
            firstQuestion = "题干",
            currentQuestionInput = "但正确答案是对",
            isFollowUp = true,
            examAnchor = anchor
        )
        assertEquals(DeepSeekAskFollowUpPipeline.FollowUpKind.ANSWER_KEY_REVIEW, resolved.kind)
        assertTrue(resolved.enableThinking)
        assertTrue(resolved.userContent!!.contains("【标答校对】"))
        assertTrue(resolved.userContent!!.contains("【题库标答】=对"))
        assertTrue(resolved.userContent!!.contains("但正确答案是对"))
    }

    @Test
    fun normalFollowUp_passesUserText() {
        val result = DeepSeekAskFollowUpPipeline.resolveNextUserContent(
            firstQuestion = "题干",
            currentQuestionInput = "请解释为何不是错",
            isFollowUp = true,
            examAnchor = anchor
        )
        assertTrue(result!!.contains("请解释为何不是错"))
    }

    @Test
    fun history_answerKeyTakesPriorityOverChallenge() {
        assertTrue(DeepSeekChatHistoryPipeline.isAnswerKeyFeedbackMessage("但正确答案是对"))
        assertFalse(DeepSeekChatHistoryPipeline.isChallengeOnlyMessage("但正确答案是对"))
        assertTrue(DeepSeekChatHistoryPipeline.isChallengeOnlyMessage("你确定吗"))
    }
}
