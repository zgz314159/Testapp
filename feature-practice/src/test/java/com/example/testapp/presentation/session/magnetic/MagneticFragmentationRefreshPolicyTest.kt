package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MagneticFragmentationRefreshPolicyTest {
    @Test
    fun latestSettingWinsAndOnlyIncompatibleDraftsAreDiscarded() {
        val saved = savedProgress(MagneticFragmentationLevel.STANDARD)

        val plan =
            MagneticFragmentationRefreshPolicy.resolve(
                savedProgress = saved,
                configuredLevel = MagneticFragmentationLevel.ATOMIZED,
            )

        assertEquals(MagneticFragmentationLevel.ATOMIZED, plan.activeLevel)
        assertTrue(plan.draftsInvalidated)
        assertEquals(emptyMap(), plan.progressToRestore?.drafts)
        assertEquals(saved.fixedQuestionOrder, plan.progressToRestore?.fixedQuestionOrder)
        assertEquals(saved.currentQuestionId, plan.progressToRestore?.currentQuestionId)
        assertEquals(saved.completedQuestionIds, plan.progressToRestore?.completedQuestionIds)
    }

    @Test
    fun matchingSettingKeepsEveryPerClauseDraft() {
        val saved = savedProgress(MagneticFragmentationLevel.FINE)

        val plan =
            MagneticFragmentationRefreshPolicy.resolve(
                savedProgress = saved,
                configuredLevel = MagneticFragmentationLevel.FINE,
            )

        assertEquals(MagneticFragmentationLevel.FINE, plan.activeLevel)
        assertFalse(plan.draftsInvalidated)
        assertEquals(saved, plan.progressToRestore)
    }

    @Test
    fun freshRoundUsesConfiguredSetting() {
        val plan =
            MagneticFragmentationRefreshPolicy.resolve(
                savedProgress = null,
                configuredLevel = MagneticFragmentationLevel.COARSE,
            )

        assertEquals(MagneticFragmentationLevel.COARSE, plan.activeLevel)
        assertEquals(null, plan.progressToRestore)
        assertFalse(plan.draftsInvalidated)
    }

    private fun savedProgress(level: MagneticFragmentationLevel): MagneticRebuildSavedProgress =
        MagneticRebuildSavedProgress(
            fixedQuestionOrder = listOf(11, 22, 33),
            currentClauseIndex = 1,
            completedQuestionIds = listOf(11),
            currentQuestionId = 22,
            drafts =
                mapOf(
                    22 to
                        MagneticSavedClauseDraft(
                            candidateTokenIds = listOf(201, 202),
                            placedTokenIds = listOf(200),
                            moveCount = 3,
                            wrongCheckCount = 1,
                            hintCount = 0,
                            originalViewCount = 0,
                            hintedTokenId = null,
                            completed = false,
                        ),
                ),
            fragmentationLevel = level,
        )
}
