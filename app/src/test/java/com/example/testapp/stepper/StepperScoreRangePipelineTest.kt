package com.example.testapp.stepper

import com.example.testapp.uicommon.component.stepper.StepperScoreRangePipeline
import org.junit.Assert.assertEquals
import org.junit.Test

class StepperScoreRangePipelineTest {
    @Test
    fun normalize_keepsMinAtMostMax() {
        assertEquals(3 to 8, StepperScoreRangePipeline.normalize(3, 8))
        assertEquals(5 to 5, StepperScoreRangePipeline.normalize(5, 3))
        assertEquals(1 to 10, StepperScoreRangePipeline.normalize(0, 99))
    }

    @Test
    fun withMin_pushesMaxUpWhenNeeded() {
        assertEquals(7 to 7, StepperScoreRangePipeline.withMin(3, 5, 7))
    }

    @Test
    fun withMax_respectsCurrentMin() {
        assertEquals(4 to 6, StepperScoreRangePipeline.withMax(4, 8, 6))
        assertEquals(4 to 4, StepperScoreRangePipeline.withMax(4, 8, 2))
    }
}
