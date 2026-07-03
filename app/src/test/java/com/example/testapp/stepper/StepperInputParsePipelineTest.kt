package com.example.testapp.stepper

import com.example.testapp.uicommon.component.stepper.StepperInputParsePipeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StepperInputParsePipelineTest {
    @Test
    fun clamp_coercesToRange() {
        assertEquals(1, StepperInputParsePipeline.clamp(0, 1, 10))
        assertEquals(10, StepperInputParsePipeline.clamp(99, 1, 10))
    }

    @Test
    fun parseDigits_filtersAndClamps() {
        assertEquals(12, StepperInputParsePipeline.parseDigits("12", 0, 99))
        assertEquals(99, StepperInputParsePipeline.parseDigits("999", 0, 99))
        assertNull(StepperInputParsePipeline.parseDigits("", 0, 99))
    }
}
