package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekMultiTurnMessagesPipelineTest {

    private val anchor = DeepSeekExamAnchor(
        questionType = "单选题",
        content = "测试题干",
        options = listOf("A", "B"),
        standardAnswer = "A",
        officialExplanation = "官方"
    )

    @Test
    fun build_includesExamSystemAndTrimsHistory() {
        val prior = (1..5).map { DeepSeekChatTurn("u$it", "a$it") }
        val messages = DeepSeekMultiTurnMessagesPipeline.build(
            priorTurns = prior,
            nextUserContent = "追问",
            examAnchor = anchor
        )
        assertTrue(messages.first().content.contains("铁路电力考试"))
        assertTrue(messages.first().content.contains("题型"))
        assertFalse(messages.first().content.contains("标准答案："))
        assertEquals(1 + 3 * 2 + 1, messages.size)
    }
}
