package com.example.testapp.data.repository.parser

import com.example.testapp.domain.QuestionTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExcelImportAnswerNormalizePipelineTest {

    @Test
    fun normalizeChoiceAnswer_concatAndDelimiters() {
        assertEquals(
            "ABC",
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(QuestionTypes.MULTI, "ABC", 4),
        )
        assertEquals(
            "ABCD",
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(QuestionTypes.MULTI, "A,B,C,D", 4),
        )
        assertEquals(
            "ABC",
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(QuestionTypes.MULTI, "A，B，C", 3),
        )
        assertEquals(
            "ABCDE",
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(QuestionTypes.MULTI, "A、B、C、D、E", 5),
        )
    }

    @Test
    fun normalizeChoiceAnswer_rejectsOverflowLetter() {
        assertNull(
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(QuestionTypes.MULTI, "ABCDEFGH", 7),
        )
    }

    @Test
    fun normalizeJudgeAnswer_aliases() {
        assertEquals("对", ExcelImportAnswerNormalizePipeline.normalizeJudgeAnswer("正确"))
        assertEquals("对", ExcelImportAnswerNormalizePipeline.normalizeJudgeAnswer("√"))
        assertEquals("错", ExcelImportAnswerNormalizePipeline.normalizeJudgeAnswer("错误"))
        assertEquals("错", ExcelImportAnswerNormalizePipeline.normalizeJudgeAnswer("×"))
        assertEquals(
            "对",
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(QuestionTypes.JUDGE, "TRUE", 2),
        )
    }

    @Test
    fun shouldSkipInstructionRow() {
        assertTrue(ExcelImportAnswerNormalizePipeline.shouldSkipInstructionRow("注意事项：请核对", ""))
        assertTrue(ExcelImportAnswerNormalizePipeline.shouldSkipInstructionRow("", "单选题"))
    }
}
