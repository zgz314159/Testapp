package com.example.testapp.presentation.screen.settings.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsStepperAccessibilityPipelineTest {

    @Test
    fun resolveOptionalCountStepperDescription_zeroUsesAllLabel() {
        assertEquals(
            "全部",
            resolveOptionalCountStepperDescription(0, "全部", "共 5 题")
        )
    }

    @Test
    fun resolveOptionalCountStepperDescription_nonZeroUsesCountedLabel() {
        assertEquals(
            "共 5 题",
            resolveOptionalCountStepperDescription(5, "全部", "共 5 题")
        )
    }

    @Test
    fun resolveScoreRangeStepperDescriptions_formatsMinAndMax() {
        val result = resolveScoreRangeStepperDescriptions(
            rangeLabel = "得分",
            minValue = 2,
            maxValue = 8,
            minTemplate = "%1\$s，最低分 %2\$d",
            maxTemplate = "%1\$s，最高分 %2\$d"
        )
        assertEquals("得分，最低分 2", result.min)
        assertEquals("得分，最高分 8", result.max)
    }
}
