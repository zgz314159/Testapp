package com.example.testapp.presentation.screen.questionbank

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionBankDrawerWidthPipelineTest {

    @Test
    fun resolveQuestionBankDrawerWidth_smallScreen_respectsMinScrim() {
        assertEquals(304, resolveQuestionBankDrawerWidth(360).value.toInt())
    }

    @Test
    fun resolveQuestionBankDrawerWidth_capsAt320() {
        assertEquals(320, resolveQuestionBankDrawerWidth(480).value.toInt())
    }
}
