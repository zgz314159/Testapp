package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekChatHistoryPipelineTest {

    @Test
    fun trimRetainedTurns_keepsLatest() {
        val turns = (1..5).map { DeepSeekChatTurn("u$it", "a$it") }
        val trimmed = DeepSeekChatHistoryPipeline.trimRetainedTurns(turns)
        assertEquals(3, trimmed.size)
        assertEquals("u3", trimmed.first().user)
        assertEquals("u5", trimmed.last().user)
    }

    @Test
    fun isChallengeOnly_detectsShortDoubts() {
        assertTrue(DeepSeekChatHistoryPipeline.isChallengeOnlyMessage("你确定吗"))
        assertTrue(DeepSeekChatHistoryPipeline.isChallengeOnlyMessage("真的吗？"))
    }

    @Test
    fun isChallengeOnly_rejectsSubstantiveFollowUp() {
        assertTrue(!DeepSeekChatHistoryPipeline.isChallengeOnlyMessage("请解释B选项"))
    }
}
