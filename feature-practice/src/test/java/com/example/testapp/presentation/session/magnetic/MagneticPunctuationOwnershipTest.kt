package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MagneticPunctuationOwnershipTest {
    @Test
    fun trailingPunctuationStaysWithPreviousChunkAndNumberedItemStartsNextChunk() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question = sampleQuestion(),
                    fragmentationLevel = MagneticFragmentationLevel.FINE,
                ),
            )
        val texts = clause.tokens.map(MagneticToken::text)

        assertEquals("应具备的基本条件：", texts.first { it.contains("基本条件") })
        assertTrue(texts.any { it.startsWith("（一）经医师鉴定") })
        assertTrue(texts.any { it.endsWith("）。") })
        assertTrue(texts.any { it.startsWith("（二）具备必要的") })
        assertFalse(texts.any(::startsWithOwnedPunctuation))
        assertFalse(texts.any { NUMBERED_ITEM_AFTER_PUNCTUATION.containsMatchIn(it) })
        assertEquals(expectedOriginalText(), clause.originalText)
    }

    @Test
    fun coarseBalancingNeverMergesAcrossNumberedItemBoundary() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question = sampleQuestion(),
                    fragmentationLevel = MagneticFragmentationLevel.COARSE,
                ),
            )

        assertFalse(clause.tokens.any { NUMBERED_ITEM_AFTER_PUNCTUATION.containsMatchIn(it.text) })
        assertFalse(clause.tokens.any { token -> startsWithOwnedPunctuation(token.text) })
        assertEquals(expectedOriginalText(), clause.originalText)
    }

    private fun sampleQuestion(): Question =
        Question(
            id = 8,
            content =
                "第8条 铁路电力作业人员____：（一）经医师鉴定，无妨碍工作的病症（____）。" +
                    "（二）具备必要的____，掌握触电急救等紧急救护技术。" +
                    "（三）具备必要的____。（四）具备必要的____。" +
                    "（五）从事____的人员应具备____。",
            type = QuestionTypes.BLANK,
            options = emptyList(),
            answer =
                listOf(
                    "应具备的基本条件",
                    "体格检查至少每两年一次",
                    "安全生产知识",
                    "电气知识和业务技能",
                    "铁路行车安全知识",
                    "高速铁路作业",
                    "掌握铁路施工作业安全要求及其系统",
                ).joinToString("\u001F"),
            explanation = "",
        )

    private fun expectedOriginalText(): String =
        "第8条 铁路电力作业人员应具备的基本条件：" +
            "（一）经医师鉴定，无妨碍工作的病症（体格检查至少每两年一次）。" +
            "（二）具备必要的安全生产知识，掌握触电急救等紧急救护技术。" +
            "（三）具备必要的电气知识和业务技能。" +
            "（四）具备必要的铁路行车安全知识。" +
            "（五）从事高速铁路作业的人员应具备掌握铁路施工作业安全要求及其系统。"

    private fun startsWithOwnedPunctuation(text: String): Boolean =
        text.trimStart().firstOrNull() in OWNED_PUNCTUATION

    private companion object {
        val OWNED_PUNCTUATION =
            setOf(
                '）', ')', '】', '》', '〉', '」', '』', '”', '’',
                '，', '。', '、', '；', '：', '！', '？', ',', '.', ';', ':', '!', '?',
            )
        val NUMBERED_ITEM_AFTER_PUNCTUATION =
            Regex("[，。、；：！？,.!?;:]\\s*[（(][一二三四五六七八九十百0-9]+[）)]")
    }
}
