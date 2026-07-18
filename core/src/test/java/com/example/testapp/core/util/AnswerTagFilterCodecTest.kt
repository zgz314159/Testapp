package com.example.testapp.core.util

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerTagFilterCodecTest {

    @Test
    fun encodeThenDecode_roundTripsMultiTags() {
        val tags = listOf("固定术语", "情态动词", "动词/谓语")
        assertEquals(tags, AnswerTagFilterCodec.decode(AnswerTagFilterCodec.encode(tags)))
    }

    @Test
    fun decode_acceptsLegacySpaceJoinedValue() {
        assertEquals(
            listOf("固定术语", "情态动词"),
            AnswerTagFilterCodec.decode("固定术语 情态动词"),
        )
    }

    @Test
    fun decode_acceptsMixedSeparators() {
        assertEquals(
            listOf("a", "b", "c", "d", "e"),
            AnswerTagFilterCodec.decode("a,b，c、d；e"),
        )
    }

    @Test
    fun encode_trimsBlanksAndDeduplicates() {
        assertEquals("固定术语、情态动词", AnswerTagFilterCodec.encode(listOf(" 固定术语 ", "", "情态动词", "固定术语")))
    }

    /** 回归：多选标签串必须能被出题过滤命中，否则练习页会「暂无题目」。 */
    @Test
    fun multiTagFilter_stillGeneratesFillVariants() {
        val question = Question(
            id = 1,
            content = "____ 与 ____ 都是重点",
            type = "填空题",
            options = emptyList(),
            answer = "part1【固定术语】\u001Fpart2【情态动词】",
            explanation = "",
        )
        val filter = AnswerTagFilterCodec.encode(listOf("固定术语", "情态动词"))
        val variants = transformQuestionVariantsForFillSettings(
            question = question,
            maxVisibleBlanks = 0,
            generationMode = FillQuestionGenerationMode.TAG_RANDOM,
            fullAnswerRandomOrder = false,
            minAnswerScore = 1,
            maxAnswerScore = 10,
            answerTagFilter = filter,
            seed = 42L,
        )
        assertTrue("多标签过滤不应过滤掉全部题目", variants.isNotEmpty())
    }

    /** 互锁：非「标签随机模式」必须忽略残留标签，切到全答模式后不得再按标签串扰出题。 */
    @Test
    fun fullAnswerMode_ignoresStaleTagFilter() {
        val question = Question(
            id = 3,
            content = "____ 与 ____ 都是重点",
            type = "填空题",
            options = emptyList(),
            answer = "part1【固定术语】\u001Fpart2【情态动词】",
            explanation = "",
        )
        val variants = transformQuestionVariantsForFillSettings(
            question = question,
            maxVisibleBlanks = 1,
            generationMode = FillQuestionGenerationMode.FULL_ANSWER,
            fullAnswerRandomOrder = false,
            minAnswerScore = 1,
            maxAnswerScore = 10,
            answerTagFilter = "固定术语",
            seed = 42L,
        )
        // 全答模式忽略标签：两个答案都应参与，每题 1 空则拆成 2 轮。
        assertEquals(2, variants.size)
    }

    /** 互锁：模式与筛选条件的适用关系由枚举属性唯一裁决。 */
    @Test
    fun modePolicy_declaresFilterApplicability() {
        assertTrue(FillQuestionGenerationMode.TAG_RANDOM.usesTagFilter)
        assertTrue(FillQuestionGenerationMode.entries.filterNot { it == FillQuestionGenerationMode.TAG_RANDOM }
            .none { it.usesTagFilter })
        assertTrue(FillQuestionGenerationMode.SCORE_RANGE_RANDOM.usesScoreRange)
        assertTrue(FillQuestionGenerationMode.FULL_ANSWER.usesScoreRange)
        assertTrue(!FillQuestionGenerationMode.TAG_RANDOM.usesScoreRange)
    }

    /** 回归：完全不匹配的标签应过滤掉该题（保持原有语义）。 */
    @Test
    fun unmatchedTagFilter_filtersQuestionOut() {
        val question = Question(
            id = 2,
            content = "____ 是重点",
            type = "填空题",
            options = emptyList(),
            answer = "part1【固定术语】",
            explanation = "",
        )
        val variants = transformQuestionVariantsForFillSettings(
            question = question,
            maxVisibleBlanks = 0,
            generationMode = FillQuestionGenerationMode.TAG_RANDOM,
            fullAnswerRandomOrder = false,
            minAnswerScore = 1,
            maxAnswerScore = 10,
            answerTagFilter = "不存在的标签",
            seed = 42L,
        )
        assertTrue(variants.isEmpty())
    }
}
