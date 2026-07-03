package com.example.testapp.uicommon.util

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class QuestionAiContextPipelineTest {

    private val question = Question(
        id = 1,
        type = "计算题",
        content = "求弛度",
        options = listOf("A项", "B项"),
        answer = "9.030m",
        explanation = ""
    )

    @Test
    fun formatQuestionForAi_includesTypeAndExcludesAnswer() {
        val text = formatQuestionForAi(question)
        assertEquals(true, text.startsWith("题型：计算题"))
        assertEquals(true, text.contains("求弛度"))
        assertEquals(true, text.contains("A. A项"))
        assertFalse(text.contains("答案"))
    }
}
