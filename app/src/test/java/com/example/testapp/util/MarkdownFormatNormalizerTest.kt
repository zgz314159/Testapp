package com.example.testapp.util

import com.example.testapp.core.util.normalizeRichMarkdownStructure
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownFormatNormalizerTest {
    @Test
    fun removesEmptyBulletPlaceholderLinesFromCalculationAnswer() {
        val raw = """
            **4. 解：**
            * *
            **已知条件：**
            * *
            * 孤立档档距：${'$'}l = 280 \text{ m}${'$'}
            * 检查实测旧弛度：${'$'}f_1 = 12.8 \text{ m}${'$'}
            * 架线标准新弛度：${'$'}f = 11.4 \text{ m}${'$'}
            **计算步骤：**
            * *
            **1. 计算弧垂变化的平方差：**
            * *
            ${'$'}${'$'}f_1^2 - f^2 = 12.8^2 - 11.4^2 = 33.88 \text{ (m}^2\text{)}${'$'}${'$'}
            **答：**
            * 该孤立档导线线长的调整量 ${'$'}\Delta L${'$'} 为 ${'$'}0.323 \text{ m}${'$'}。
        """.trimIndent()

        val normalized = normalizeRichMarkdownStructure(raw)

        assertTrue(normalized.contains("**4. 解：**\n**已知条件：**"))
        assertTrue(normalized.contains("**计算步骤：**\n**1. 计算弧垂变化的平方差：**"))
        assertTrue(normalized.contains("**答：**\n* 该孤立档导线线长的调整量"))
        assertFalse(normalized.lineSequence().any { it.trim() == "* *" })
        assertFalse(normalized.lineSequence().any { it.trim() == "*" })
        assertFalse(normalized.lineSequence().any { it.trim() == "**" })
    }

    @Test
    fun removesOrphanBoldMarkersAfterInlineMathInBulletLine() {
        val raw = "* ${'$'}8487.05 \\text{ N}${'$'}**（${'$'}866.03 \\text{ kgf}${'$'}）。\n```"
        val normalized = normalizeRichMarkdownStructure(raw)

        assertTrue(normalized.contains("${'$'}8487.05 \\text{ N}${'$'}（${'$'}866.03 \\text{ kgf}${'$'}）"))
        assertFalse(normalized.contains("**"))
        assertFalse(normalized.contains("```"))
    }
}
