package com.example.testapp.presentation.screen.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAdaptiveModeEligibilityPipelineTest {
    @Test
    fun `only sqlite and db files expose adaptive mode`() {
        assertTrue(HomeAdaptiveModeEligibilityPipeline.isEligible("安规.sqlite"))
        assertTrue(HomeAdaptiveModeEligibilityPipeline.isEligible("安规.DB"))
        assertFalse(HomeAdaptiveModeEligibilityPipeline.isEligible("普通题库.xlsx"))
        assertFalse(HomeAdaptiveModeEligibilityPipeline.isEligible("题库.json"))
    }
}
