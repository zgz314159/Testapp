package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.core.util.FILL_PART_DELIMITER
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MagneticRebuildPrepareLimitTest {
    @Test
    fun prepareStopsAfterRequestedSessionSize() {
        val questions =
            (1..80).map { index ->
                Question(
                    id = index,
                    content = "第${index}条 ____和____、____。",
                    type = QuestionTypes.BLANK,
                    options = emptyList(),
                    answer =
                        listOf("甲【2分】", "乙【2分】", "丙【2分】")
                            .joinToString(FILL_PART_DELIMITER),
                    explanation = "",
                )
            }
        val clauses =
            MagneticRebuildQuestionPipeline.prepare(
                sourceQuestions = questions,
                requestedCount = 20,
                randomOrder = false,
                seed = 1L,
                fragmentationLevel = MagneticFragmentationLevel.SOURCE_ATOMIC,
            )
        assertEquals(20, clauses.size)
        assertEquals((1..20).toList(), clauses.map(MagneticClause::sourceQuestionId))
    }

    @Test
    fun prepareFixedOrderOnlyBuildsListedQuestions() {
        val questions =
            (1..40).map { index ->
                Question(
                    id = index,
                    content = "条文____ ____ ____。",
                    type = QuestionTypes.BLANK,
                    options = emptyList(),
                    answer =
                        listOf("一【2分】", "二【2分】", "三【2分】")
                            .joinToString(FILL_PART_DELIMITER),
                    explanation = "",
                )
            }
        val clauses =
            MagneticRebuildQuestionPipeline.prepare(
                sourceQuestions = questions,
                requestedCount = 20,
                randomOrder = true,
                seed = 9L,
                fixedQuestionOrder = listOf(30, 5, 12),
                fragmentationLevel = MagneticFragmentationLevel.ATOMIZED,
            )
        assertEquals(listOf(30, 5, 12), clauses.map(MagneticClause::sourceQuestionId))
        assertTrue(clauses.all { it.tokens.size >= 3 })
    }
}
