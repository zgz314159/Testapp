package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeProgressIdPipelineTest {
    @Test
    fun ensurePrefix_addsWhenMissing() {
        assertEquals("practice_quiz1", PracticeProgressIdPipeline.ensurePrefix("quiz1"))
    }

    @Test
    fun ensurePrefix_keepsExisting() {
        assertEquals("practice_quiz1", PracticeProgressIdPipeline.ensurePrefix("practice_quiz1"))
    }
}
