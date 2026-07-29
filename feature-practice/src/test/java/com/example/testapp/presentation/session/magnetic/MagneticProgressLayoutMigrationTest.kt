package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.UnifiedQuestionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MagneticProgressLayoutMigrationTest {
    @Test
    fun v2DraftsAreDiscardedWhenPunctuationOwnershipChanges() {
        val decoded =
            assertNotNull(
                MagneticRebuildProgressCodec.decode(
                    PracticeProgress(
                        id = "magnetic_bank",
                        currentIndex = 2,
                        answeredList = listOf(11),
                        selectedOptions = emptyList(),
                        showResultList = emptyList(),
                        analysisList = listOf("33"),
                        noteList = listOf("2"),
                        timestamp = 1L,
                        sessionId = "magnetic-rebuild-v2",
                        fixedQuestionOrder = listOf(11, 22, 33),
                        questionStateMap =
                            mapOf(
                                33 to
                                    UnifiedQuestionState(
                                        questionId = 33,
                                        selectedOptions = listOf(3300),
                                        textAnswer = "3301,3302",
                                        showResult = false,
                                        analysis = "3,0,0,0,-1",
                                    ),
                            ),
                    ),
                ),
            )

        assertTrue(decoded.drafts.isEmpty())
        assertEquals(listOf(11, 22, 33), decoded.fixedQuestionOrder)
        assertEquals(listOf(11), decoded.completedQuestionIds)
        assertEquals(33, decoded.currentQuestionId)
        assertEquals(2, decoded.currentClauseIndex)
    }

    @Test
    fun currentFormatKeepsDrafts() {
        val saved =
            MagneticRebuildSavedProgress(
                fixedQuestionOrder = listOf(11, 22),
                currentClauseIndex = 1,
                completedQuestionIds = listOf(11),
                currentQuestionId = 22,
                drafts =
                    mapOf(
                        22 to
                            MagneticSavedClauseDraft(
                                candidateTokenIds = listOf(2201),
                                placedTokenIds = listOf(2200),
                                moveCount = 1,
                                wrongCheckCount = 0,
                                hintCount = 0,
                                originalViewCount = 0,
                                hintedTokenId = null,
                                completed = false,
                            ),
                    ),
                fragmentationLevel = MagneticFragmentationLevel.STANDARD,
            )
        val encoded = MagneticRebuildProgressCodec.encode("bank", saved)
        val decoded = assertNotNull(MagneticRebuildProgressCodec.decode(encoded))

        assertEquals("magnetic-rebuild-v3", encoded.sessionId)
        assertEquals(saved.drafts, decoded.drafts)
    }
}
