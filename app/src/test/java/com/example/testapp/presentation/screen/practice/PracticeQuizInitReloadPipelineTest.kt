package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeQuizInitReloadPipelineTest {

    @Test
    fun skipReload_whenSessionActiveAndInitKeyUnchanged() {
        assertFalse(
            PracticeQuizInitReloadPipeline.shouldReloadFillConfig(
                sessionActive = true,
                appliedInitKey = "fill|20|true",
                currentInitKey = "fill|20|true"
            )
        )
    }

    @Test
    fun reload_whenFillConfigChanged() {
        assertTrue(
            PracticeQuizInitReloadPipeline.shouldReloadFillConfig(
                sessionActive = true,
                appliedInitKey = "fill|20|true",
                currentInitKey = "fill|20|false"
            )
        )
    }

    @Test
    fun reload_whenSessionNotActive() {
        assertTrue(
            PracticeQuizInitReloadPipeline.shouldReloadFillConfig(
                sessionActive = false,
                appliedInitKey = "fill|20|true",
                currentInitKey = "fill|20|true"
            )
        )
    }
}
