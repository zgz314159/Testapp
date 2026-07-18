package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatCitationStylePipelineTest {

    @Test
    fun ranges_findsAllCitationMarkers() {
        val text = "主题为“网络安全为人民” [1][4]，2021 年确认 [12]。"
        val ranges = AiChatCitationStylePipeline.ranges(text)

        assertEquals(3, ranges.size)
        assertEquals("[1]", text.substring(ranges[0].first, ranges[0].last + 1))
        assertEquals("[4]", text.substring(ranges[1].first, ranges[1].last + 1))
        assertEquals("[12]", text.substring(ranges[2].first, ranges[2].last + 1))
    }

    @Test
    fun ranges_ignoresNonCitationBrackets() {
        assertTrue(AiChatCitationStylePipeline.ranges("选项 [A] 与 [abc] 不是引用").isEmpty())
    }

    @Test
    fun sectionTitleRanges_findsLineLeadingLabels() {
        val text = "最终答案：B\n\n依据：根据讲话内容 [1]。\n\n解析：题目考查搭配关系。"
        val ranges = AiChatCitationStylePipeline.sectionTitleRanges(text)

        assertEquals(3, ranges.size)
        assertEquals("最终答案：", text.substring(ranges[0].first, ranges[0].last + 1))
        assertEquals("依据：", text.substring(ranges[1].first, ranges[1].last + 1))
        assertEquals("解析：", text.substring(ranges[2].first, ranges[2].last + 1))
    }

    @Test
    fun sectionTitleRanges_ignoresLongLeadingText() {
        val text = "这是一段超过十个字的普通句子开头：后面才有冒号"
        assertTrue(AiChatCitationStylePipeline.sectionTitleRanges(text).isEmpty())
    }
}
