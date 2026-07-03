package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerResultPreviewPipelineTest {

    @Test
    fun resolveAnswerResultPreviewLine_collapsesWhitespace() {
        assertEquals(
            "回答错误！ A",
            resolveAnswerResultPreviewLine("回答错误！\nA")
        )
    }

    @Test
    fun resolveAnswerResultPreviewLine_truncatesLongText() {
        val long = "a".repeat(100)
        assertEquals(96, resolveAnswerResultPreviewLine(long).length)
        assertEquals('…', resolveAnswerResultPreviewLine(long).last())
    }
}
