package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeSessionStartIndexPipelineTest {

    @Test
    fun preserveCurrentIndex_overridesRandom() {
        assertEquals(
            35,
            PracticeSessionStartIndexPipeline.resolve(
                questionCount = 200,
                restoreFromMap = false,
                savedCurrentIndex = null,
                randomPracticeEnabled = true,
                sessionStartTime = 1L,
                preserveCurrentIndex = 35
            )
        )
    }

    @Test
    fun restoreFromMap_usesSavedIndex() {
        assertEquals(
            11,
            PracticeSessionStartIndexPipeline.resolve(
                questionCount = 50,
                restoreFromMap = true,
                savedCurrentIndex = 11,
                randomPracticeEnabled = true,
                sessionStartTime = 1L,
                preserveCurrentIndex = null
            )
        )
    }
}
