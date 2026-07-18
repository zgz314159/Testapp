package com.example.testapp.uicommon.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionEditFillUtilsTest {

    @Test
    fun insertEditableBlankAtCursor_insertsUnderlineAtCursor() {
        val result = insertEditableBlankAtCursor("前半段后半段", cursor = 3)

        assertEquals("前半段 ____ 后半段", result.content)
        assertEquals(0, result.blankIndex)
        assertEquals(8, result.cursorPosition)
    }

    @Test
    fun insertEditableChoiceBlankAtCursor_insertsParenthesesAtCursor() {
        val result = insertEditableChoiceBlankAtCursor("涉密人员离岗离职实行管理。", cursor = 10)

        assertEquals("涉密人员离岗离职实行 ( ) 管理。", result.content)
        assertEquals(0, result.blankIndex)
        assertEquals(14, result.cursorPosition)
    }

    @Test
    fun insertEditableChoiceBlankAtCursor_countsExistingBlanksBeforeCursor() {
        val result = insertEditableChoiceBlankAtCursor("甲 ( ) 乙丙", cursor = 7)

        assertEquals("甲 ( ) 乙 ( ) 丙", result.content)
        assertEquals(1, result.blankIndex)
    }

    @Test
    fun parseEditableFillAnswerFields_splitsTagAndScore() {
        val fields = parseEditableFillAnswerFields("人身安全【固定术语】【10分】")

        assertEquals("人身安全", fields.answerText)
        assertEquals("固定术语", fields.tag)
        assertEquals("10", fields.score)
    }

    @Test
    fun parseEditableFillAnswerFields_plainAnswerHasEmptyTagAndScore() {
        val fields = parseEditableFillAnswerFields("人身安全")

        assertEquals("人身安全", fields.answerText)
        assertEquals("", fields.tag)
        assertEquals("", fields.score)
    }

    @Test
    fun buildEditableFillAnswerPart_roundTripsStorageFormat() {
        val raw = buildEditableFillAnswerPart("人身安全", "固定术语", "10")

        assertEquals("人身安全【固定术语】【10分】", raw)
        assertEquals(
            EditableFillAnswerFields("人身安全", "固定术语", "10"),
            parseEditableFillAnswerFields(raw),
        )
    }

    @Test
    fun buildEditableFillAnswerPart_blankAnswerDropsSuffixes() {
        assertEquals("", buildEditableFillAnswerPart("", "固定术语", "8"))
    }

    @Test
    fun buildEditableFillAnswerPart_coercesScoreIntoStorageRange() {
        assertEquals("答案【10分】", buildEditableFillAnswerPart("答案", "", "15"))
        assertEquals("答案【1分】", buildEditableFillAnswerPart("答案", "", "0"))
        assertEquals("答案", buildEditableFillAnswerPart("答案", "", ""))
    }
}
