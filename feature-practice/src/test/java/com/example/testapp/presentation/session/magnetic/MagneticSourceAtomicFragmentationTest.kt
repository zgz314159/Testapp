package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.core.util.FILL_PART_DELIMITER
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MagneticSourceAtomicFragmentationTest {
    @Test
    fun atomizedFineAbsorbsGapsIntoScoredCores() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "第4条 ____ ____ ____，____和____。",
                            answers =
                                listOf(
                                    "各级管理部门【主体】【2分】",
                                    "应加强【动作】【2分】",
                                    "电力安全生产管理【对象】【2分】",
                                    "行车安全【对象】【2分】",
                                    "设备安全【对象】【2分】",
                                ),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.ATOMIZED,
                ),
            )

        assertEquals(
            listOf(
                "第4条各级管理部门",
                "应加强",
                "电力安全生产管理，",
                "行车安全和",
                "设备安全。",
            ),
            clause.tokens.map(MagneticToken::text),
        )
        assertFalse(clause.tokens.any { it.text in setOf("和", "，", "。") })
    }

    @Test
    fun sourceAtomicKeepsBankGapsAsSeparateChunks() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "第4条 ____ ____ ____，____和____。",
                            answers =
                                listOf(
                                    "各级管理部门【主体】【2分】",
                                    "应加强【动作】【2分】",
                                    "电力安全生产管理【对象】【2分】",
                                    "行车安全【对象】【2分】",
                                    "设备安全【对象】【2分】",
                                ),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.SOURCE_ATOMIC,
                ),
            )

        assertEquals(
            listOf(
                "第4条",
                "各级管理部门",
                "应加强",
                "电力安全生产管理，",
                "行车安全",
                "和",
                "设备安全。",
            ),
            clause.tokens.map(MagneticToken::text),
        )
        assertTrue(clause.tokens.any { it.text == "和" })
    }

    @Test
    fun atomizedZeroScorePrefixAndSuffixAttachToNeighbors() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "____在____中，____。",
                            answers = listOf("作业人员【2分】", "现场工作【2分】", "应采取措施【2分】"),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.ATOMIZED,
                ),
            )

        assertEquals(
            listOf("作业人员", "在现场工作中，", "应采取措施。"),
            clause.tokens.map(MagneticToken::text),
        )
    }

    @Test
    fun sourceAtomicKeepsPrefixGapsSeparateLikeBankLayout() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "____在____中，____。",
                            answers = listOf("作业人员【2分】", "现场工作【2分】", "应采取措施【2分】"),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.SOURCE_ATOMIC,
                ),
            )

        assertEquals(
            listOf("作业人员", "在", "现场工作", "中，", "应采取措施。"),
            clause.tokens.map(MagneticToken::text),
        )
    }

    @Test
    fun atomizedNumberedItemStartsNextAtomWhileSentencePunctuationStaysPrevious() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "____。（二）____；____。",
                            answers = listOf("掌握安全知识【2分】", "具备业务技能【2分】", "熟悉设备【2分】"),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.ATOMIZED,
                ),
            )

        assertEquals(
            listOf("掌握安全知识。", "（二）具备业务技能；", "熟悉设备。"),
            clause.tokens.map(MagneticToken::text),
        )
    }

    @Test
    fun sourceAtomicIsNotCappedAndKeepsConnectorsBetweenAtoms() {
        val answerParts = (1..26).map { index -> "原子$index【2分】" }
        val content = answerParts.indices.joinToString(separator = "和") { "____" } + "。"
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question = question(content = content, answers = answerParts),
                    fragmentationLevel = MagneticFragmentationLevel.SOURCE_ATOMIC,
                ),
            )

        assertEquals(51, clause.tokens.size) // 26 atoms + 25 connectors, trailing 。 on last
        assertEquals("原子1", clause.tokens.first().text)
        assertEquals("和", clause.tokens[1].text)
        assertEquals("原子26。", clause.tokens.last().text)
    }

    @Test
    fun atomizedZeroScoreAnswerAtomsAreAbsorbedIntoNeighborCores() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "____ ____ ____ ____。",
                            answers =
                                listOf(
                                    "设备安全【对象】【2分】",
                                    "的【0分】",
                                    "日常巡视【动作】【2分】",
                                    "检查【动作】【2分】",
                                ),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.ATOMIZED,
                ),
            )

        assertEquals(
            listOf("设备安全的", "日常巡视", "检查。"),
            clause.tokens.map(MagneticToken::text),
        )
        assertFalse(clause.tokens.any { it.text == "的" })
    }

    @Test
    fun sourceAtomicKeepsZeroScoreAnswerAtomsAsOwnChunks() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "____ ____ ____ ____。",
                            answers =
                                listOf(
                                    "设备安全【对象】【2分】",
                                    "的【0分】",
                                    "日常巡视【动作】【2分】",
                                    "检查【动作】【2分】",
                                ),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.SOURCE_ATOMIC,
                ),
            )

        assertEquals(
            listOf("设备安全", "的", "日常巡视", "检查。"),
            clause.tokens.map(MagneticToken::text),
        )
    }

    @Test
    fun atomizedConnectorZeroScoreAtomsStickToPreviousCore() {
        val clause =
            assertNotNull(
                MagneticRebuildQuestionPipeline.buildClause(
                    question =
                        question(
                            content = "____ ____ ____ ____ ____。",
                            answers =
                                listOf(
                                    "行车安全【2分】",
                                    "和【0分】",
                                    "设备安全【2分】",
                                    "并【0分】",
                                    "人身安全【2分】",
                                ),
                        ),
                    fragmentationLevel = MagneticFragmentationLevel.ATOMIZED,
                ),
            )

        assertEquals(
            listOf("行车安全和", "设备安全并", "人身安全。"),
            clause.tokens.map(MagneticToken::text),
        )
    }

    private fun question(
        content: String,
        answers: List<String>,
    ): Question =
        Question(
            id = 404,
            content = content,
            type = QuestionTypes.BLANK,
            options = emptyList(),
            answer = answers.joinToString(FILL_PART_DELIMITER),
            explanation = "",
        )
}
