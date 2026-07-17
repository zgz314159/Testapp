package com.example.testapp.presentation.session.adaptive

import com.example.testapp.domain.model.AdaptiveAtomPool
import com.example.testapp.domain.model.AdaptiveAtomStage
import com.example.testapp.domain.model.AdaptiveAtomState
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveAtomProgressionPipelineTest {
    private val initial =
        AdaptiveAtomState(
            bankId = "bank.sqlite",
            atomId = -1,
            sourceQuestionId = 1,
            blankIndex = 0,
            tag = "强制性动词",
            weight = 9,
            pool = AdaptiveAtomPool.CORE,
        )

    @Test
    fun `two correct choice reviews advance to hinted`() {
        val first = AdaptiveAtomProgressionPipeline.next(initial, correct = true, reviewedAt = 1_000L)
        val second = AdaptiveAtomProgressionPipeline.next(first, correct = true, reviewedAt = 2_000L)

        assertEquals(AdaptiveAtomStage.HINTED, second.stage)
        assertEquals(0, second.correctStreak)
        assertEquals(2, second.reviewCount)
        assertEquals(86_402_000L, second.dueAt)
    }

    @Test
    fun `wrong mature atom regresses and is promoted to core`() {
        val mature =
            initial.copy(
                pool = AdaptiveAtomPool.PROBE,
                stage = AdaptiveAtomStage.MATURE,
                correctStreak = 4,
            )

        val next = AdaptiveAtomProgressionPipeline.next(mature, correct = false, reviewedAt = 5_000L)

        assertEquals(AdaptiveAtomPool.CORE, next.pool)
        assertEquals(AdaptiveAtomStage.RECALL, next.stage)
        assertEquals(0, next.correctStreak)
        assertEquals(1, next.lapseCount)
        assertEquals(605_000L, next.dueAt)
    }
}
