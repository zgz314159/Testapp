package com.example.testapp.presentation.session.adaptive

import com.example.testapp.core.util.FILL_PART_DELIMITER
import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.AdaptiveAtomPool
import com.example.testapp.domain.model.AdaptiveAtomState
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveFadingQuestionPipelineTest {
    @Test
    fun `session keeps one atom per article and reserves probe coverage`() {
        val questions = (1..20).map(::atomicQuestion)

        val prepared =
            AdaptiveFadingQuestionPipeline.prepare(
                bankId = "bank.sqlite",
                sourceQuestions = questions,
                storedStates = emptyList(),
                requestedCount = 20,
                now = 86_400_000L,
            )

        assertEquals(20, prepared.questions.size)
        assertEquals(20, prepared.states.map { it.sourceQuestionId }.distinct().size)
        assertEquals(3, prepared.states.count { it.pool == AdaptiveAtomPool.PROBE })
        assertTrue(prepared.questions.all { it.type == QuestionTypes.SINGLE })
        assertTrue(prepared.questions.all { question -> Regex("【\\s*】").findAll(question.content).count() == 1 })
    }

    @Test
    fun `choice without enough same-tag distractors falls back to hinted recall`() {
        val prepared =
            AdaptiveFadingQuestionPipeline.prepare(
                bankId = "bank.sqlite",
                sourceQuestions =
                    listOf(
                        Question(
                            id = 1,
                            content = "第1条 【   】。",
                            type = QuestionTypes.BLANK,
                            options = emptyList(),
                            answer = "必须【强制性动词】【10分】",
                            explanation = "安规 第1条",
                        ),
                    ),
                storedStates = emptyList(),
                requestedCount = 1,
                now = 0L,
            )

        assertEquals(QuestionTypes.BLANK, prepared.questions.single().type)
        assertTrue(prepared.questions.single().content.contains("提示："))
    }

    @Test
    fun `reviewed due atom is selected before unseen atom`() {
        val reviewed =
            AdaptiveAtomState(
                bankId = "bank.sqlite",
                atomId = buildDerivedFillQuestionId(1, 0),
                sourceQuestionId = 1,
                blankIndex = 0,
                tag = "一般知识",
                weight = 10,
                pool = AdaptiveAtomPool.CORE,
                reviewCount = 1,
                dueAt = 500L,
            )

        val prepared =
            AdaptiveFadingQuestionPipeline.prepare(
                bankId = "bank.sqlite",
                sourceQuestions = listOf(atomicQuestion(1), atomicQuestion(2)),
                storedStates = listOf(reviewed),
                requestedCount = 1,
                now = 1_000L,
            )

        assertEquals(1, prepared.states.single().sourceQuestionId)
    }

    @Test
    fun `numeric choice distractors keep the same unit`() {
        val questions =
            listOf(
                numericQuestion(1, "10%"),
                numericQuestion(2, "20米"),
                numericQuestion(3, "30%"),
                numericQuestion(4, "40%"),
            )

        val prepared =
            AdaptiveFadingQuestionPipeline.prepare(
                bankId = "bank.sqlite",
                sourceQuestions = questions,
                storedStates = emptyList(),
                requestedCount = 1,
                now = 0L,
            )

        assertEquals(QuestionTypes.SINGLE, prepared.questions.single().type)
        assertTrue(prepared.questions.single().options.all { it.endsWith("%") })
    }

    private fun atomicQuestion(id: Int): Question {
        val answers =
            listOf(
                "必须$id【一般知识】【10分】",
                "确认$id【一般知识】【9分】",
                "防护$id【一般知识】【8分】",
                "记录$id【一般知识】【1分】",
            ).joinToString(FILL_PART_DELIMITER)
        return Question(
            id = id,
            content = "第${id}条 【   】、【   】、【   】、【   】。",
            type = QuestionTypes.BLANK,
            options = emptyList(),
            answer = answers,
            explanation = "安规 第${id}条",
        )
    }

    private fun numericQuestion(id: Int, answer: String) =
        Question(
            id = id,
            content = "第${id}条 【   】。",
            type = QuestionTypes.BLANK,
            options = emptyList(),
            answer = "$answer【数据参数】【10分】",
            explanation = "安规 第${id}条",
        )
}
